package com.issue.importer.cdk.construct;

import software.amazon.awscdk.CfnCondition;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ec2.CfnSecurityGroup;
import software.amazon.awscdk.services.ec2.CfnSecurityGroupIngress;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.CfnService;
import software.amazon.awscdk.services.ecs.CfnTaskDefinition;
import software.amazon.awscdk.services.elasticloadbalancingv2.CfnListenerRule;
import software.amazon.awscdk.services.elasticloadbalancingv2.CfnTargetGroup;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.*;

/**
 * Creates an ECS service on top of a {@link Network}. Loads Docker images from a {@link DockerImageSource}
 * and places them into ECS task. Creates a log group for each ECS task. Creates a target group for the ECS tasks
 * that is attached to the load balancer from the {@link Network}.
 */
public class Service extends Construct {

    public Service(
            final Construct scope,
            final String id,
            final Environment awsEnvironment,
            final ApplicationEnvironment applicationEnvironment,
            final ServiceInputParameters serviceInputParameters,
            final Network.NetworkOutputParameters networkOutputParameters) {
        super(scope, id);

        List<CfnTargetGroup.TargetGroupAttributeProperty> stickySessionConfiguration = Arrays.asList(
                CfnTargetGroup.TargetGroupAttributeProperty.builder().key("stickiness.enabled").value("true").build(),
                CfnTargetGroup.TargetGroupAttributeProperty.builder().key("stickiness.type").value("lb_cookie").build(),
                CfnTargetGroup.TargetGroupAttributeProperty.builder().key("stickiness.lb_cookie.duration_seconds").value("3600").build()
        );
        List<CfnTargetGroup.TargetGroupAttributeProperty> deregistrationDelayConfiguration = List.of(
                CfnTargetGroup.TargetGroupAttributeProperty.builder().key("deregistration_delay.timeout_seconds").value("5").build()
        );
        List<CfnTargetGroup.TargetGroupAttributeProperty> targetGroupAttributes = new ArrayList<>(deregistrationDelayConfiguration);
        if (ServiceInputParameters.PARAMETER_STICKY_SESSIONS_ENABLED) {
            targetGroupAttributes.addAll(stickySessionConfiguration);
        }

        CfnTargetGroup targetGroup = CfnTargetGroup.Builder.create(this, "targetGroup")
                .healthCheckIntervalSeconds(ServiceInputParameters.PARAMETER_HEALTH_CHECK_INTERVAL_SECONDS)
                .healthCheckPath(serviceInputParameters.parameterHealthCheckPath)
                .healthCheckPort(String.valueOf(ServiceInputParameters.PARAMETER_CONTAINER_PORT))
                .healthCheckProtocol(ServiceInputParameters.PARAMETER_CONTAINER_PROTOCOL)
                .healthCheckTimeoutSeconds(ServiceInputParameters.PARAMETER_HEALTH_CHECK_TIMEOUT_SECONDS)
                .healthyThresholdCount(ServiceInputParameters.PARAMETER_HEALTHY_THRESHOLD_COUNT)
                .unhealthyThresholdCount(ServiceInputParameters.PARAMETER_UNHEALTHY_THRESHOLD_COUNT)
                .targetGroupAttributes(targetGroupAttributes)
                .targetType("ip")
                .port(ServiceInputParameters.PARAMETER_CONTAINER_PORT)
                .protocol(ServiceInputParameters.PARAMETER_CONTAINER_PROTOCOL)
                .vpcId(networkOutputParameters.getVpcId())
                .build();

        CfnListenerRule.ActionProperty actionProperty = CfnListenerRule.ActionProperty.builder()
                .targetGroupArn(targetGroup.getRef())
                .type("forward")
                .build();

        CfnListenerRule.RuleConditionProperty condition = CfnListenerRule.RuleConditionProperty.builder()
                .field("path-pattern")
                .values(Collections.singletonList("*"))
                .build();

        Optional<String> httpsListenerArn = networkOutputParameters.getHttpsListenerArn();

        // We only want the HTTPS listener to be deployed if the httpsListenerArn is present.
        if (httpsListenerArn.isPresent()) {
            CfnListenerRule httpsListenerRule = CfnListenerRule.Builder.create(this, "httpsListenerRule")
                    .actions(Collections.singletonList(actionProperty))
                    .conditions(Collections.singletonList(condition))
                    .listenerArn(httpsListenerArn.get())
                    .priority(1)
                    .build();

            CfnCondition httpsListenerRuleCondition = CfnCondition.Builder.create(this, "httpsListenerRuleCondition")
                    .expression(Fn.conditionNot(Fn.conditionEquals(httpsListenerArn.get(), "null")))
                    .build();

            httpsListenerRule.getCfnOptions().setCondition(httpsListenerRuleCondition);
        }

        CfnListenerRule httpListenerRule = CfnListenerRule.Builder.create(this, "httpListenerRule")
                .actions(Collections.singletonList(actionProperty))
                .conditions(Collections.singletonList(condition))
                .listenerArn(networkOutputParameters.getHttpListenerArn())
                .priority(2)
                .build();

        LogGroup logGroup = LogGroup.Builder.create(this, "ecsLogGroup")
                .logGroupName(applicationEnvironment.prefix("logs"))
                .retention(ServiceInputParameters.PARAMETER_RETENTION_DAYS)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        Role ecsTaskExecutionRole = Role.Builder.create(this, "ecsTaskExecutionRole")
                .assumedBy(ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build())
                .path("/")
                .inlinePolicies(Map.of(
                        applicationEnvironment.prefix("ecsTaskExecutionRolePolicy"),
                        PolicyDocument.Builder.create()
                                .statements(Collections.singletonList(PolicyStatement.Builder.create()
                                        .effect(Effect.ALLOW)
                                        .resources(Collections.singletonList("*"))
                                        .actions(Arrays.asList(
                                                "ecr:GetAuthorizationToken",
                                                "ecr:BatchCheckLayerAvailability",
                                                "ecr:GetDownloadUrlForLayer",
                                                "ecr:BatchGetImage",
                                                "logs:CreateLogStream",
                                                "logs:PutLogEvents"))
                                        .build()))
                                .build()))
                .build();

        Role.Builder roleBuilder = Role.Builder.create(this, "ecsTaskRole")
                .assumedBy(ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build())
                .path("/");

        if (!serviceInputParameters.taskRolePolicyStatements.isEmpty()) {
            roleBuilder.inlinePolicies(Map.of(
                    applicationEnvironment.prefix("ecsTaskRolePolicy"),
                    PolicyDocument.Builder.create()
                            .statements(serviceInputParameters.taskRolePolicyStatements)
                            .build()));
        }

        Role ecsTaskRole = roleBuilder.build();

        String dockerRepositoryUrl;
        IRepository dockerRepository = Repository.fromRepositoryName(this, "ecrRepository", serviceInputParameters.dockerImageSource.getDockerRepositoryName());
        dockerRepository.grantPull(ecsTaskExecutionRole);
        dockerRepositoryUrl = dockerRepository.repositoryUriForTag(serviceInputParameters.dockerImageSource.getDockerImageTag());

        CfnTaskDefinition.ContainerDefinitionProperty container = CfnTaskDefinition.ContainerDefinitionProperty.builder()
                .name(containerName(applicationEnvironment))
                .cpu(ServiceInputParameters.PARAMETER_CPU)
                .memory(ServiceInputParameters.PARAMETER_MEMORY)
                .image(dockerRepositoryUrl)
                .logConfiguration(CfnTaskDefinition.LogConfigurationProperty.builder()
                        .logDriver("awslogs")
                        .options(Map.of(
                                "awslogs-group", logGroup.getLogGroupName(),
                                "awslogs-region", Objects.requireNonNull(awsEnvironment.getRegion()),
                                "awslogs-stream-prefix", applicationEnvironment.prefix("stream"),
                                "awslogs-datetime-format", ServiceInputParameters.PARAMETER_AWSLOGS_DATE_TIME_FORMAT))
                        .build())
                .portMappings(Collections.singletonList(CfnTaskDefinition.PortMappingProperty.builder()
                        .containerPort(ServiceInputParameters.PARAMETER_CONTAINER_PORT)
                        .build()))
                .environment(toKeyValuePairs(serviceInputParameters.environmentVariables))
                .stopTimeout(2)
                .build();

        CfnTaskDefinition taskDefinition = CfnTaskDefinition.Builder.create(this, "taskDefinition")
                // skipped family
                .cpu(String.valueOf(ServiceInputParameters.PARAMETER_CPU))
                .memory(String.valueOf(ServiceInputParameters.PARAMETER_MEMORY))
                .networkMode("awsvpc")
                .requiresCompatibilities(Collections.singletonList("FARGATE"))
                .executionRoleArn(ecsTaskExecutionRole.getRoleArn())
                .taskRoleArn(ecsTaskRole.getRoleArn())
                .containerDefinitions(Collections.singletonList(container))
                .build();

        CfnSecurityGroup ecsSecurityGroup = CfnSecurityGroup.Builder.create(this, "ecsSecurityGroup")
                .vpcId(networkOutputParameters.getVpcId())
                .groupDescription("SecurityGroup for the ECS containers")
                .build();

        // allow ECS containers to access each other
        CfnSecurityGroupIngress.Builder.create(this, "ecsIngressFromSelf")
                .ipProtocol("-1")
                .sourceSecurityGroupId(ecsSecurityGroup.getAttrGroupId())
                .groupId(ecsSecurityGroup.getAttrGroupId())
                .build();

        // allow the load balancer to access the containers
        CfnSecurityGroupIngress.Builder.create(this, "ecsIngressFromLoadBalancer")
                .ipProtocol("-1")
                .sourceSecurityGroupId(networkOutputParameters.getLoadBalancerSecurityGroupId())
                .groupId(ecsSecurityGroup.getAttrGroupId())
                .build();

        allowIngressFromEcs(serviceInputParameters.securityGroupIdsToGrantIngressFromEcs, ecsSecurityGroup);

        CfnService service = CfnService.Builder.create(this, "ecsService")
                .cluster(networkOutputParameters.getEcsClusterName())
                .launchType("FARGATE")
                .deploymentConfiguration(CfnService.DeploymentConfigurationProperty.builder()
                        .maximumPercent(ServiceInputParameters.PARAMETER_MAXIMUM_INSTANCES_PERCENT)
                        .minimumHealthyPercent(ServiceInputParameters.PARAMETER_MINIMUM_HEALTHY_INSTANCES_PERCENT)
                        .build())
                .desiredCount(ServiceInputParameters.PARAMETER_DESIRED_INSTANCES_COUNT)
                .taskDefinition(taskDefinition.getRef())
                .loadBalancers(Collections.singletonList(CfnService.LoadBalancerProperty.builder()
                        .containerName(containerName(applicationEnvironment))
                        .containerPort(ServiceInputParameters.PARAMETER_CONTAINER_PORT)
                        .targetGroupArn(targetGroup.getRef())
                        .build()))
                .networkConfiguration(CfnService.NetworkConfigurationProperty.builder()
                        .awsvpcConfiguration(CfnService.AwsVpcConfigurationProperty.builder()
                                .assignPublicIp("ENABLED")
                                .securityGroups(Collections.singletonList(ecsSecurityGroup.getAttrGroupId()))
                                .subnets(networkOutputParameters.getPublicSubnets())
                                .build())
                        .build())
                .build();

        // Adding an explicit dependency from the service to the listeners to avoid "has no load balancer associated" error
        // (see https://stackoverflow.com/questions/61250772/how-can-i-create-a-dependson-relation-between-ec2-and-rds-using-aws-cdk).
        service.addDependency(httpListenerRule);

        applicationEnvironment.tag(this);
    }

    private void allowIngressFromEcs(List<String> securityGroupIds, CfnSecurityGroup ecsSecurityGroup) {
        int i = 1;
        for (String securityGroupId : securityGroupIds) {
            CfnSecurityGroupIngress.Builder.create(this, "securityGroupIngress" + i)
                    .sourceSecurityGroupId(ecsSecurityGroup.getAttrGroupId())
                    .groupId(securityGroupId)
                    .ipProtocol("-1")
                    .build();
            i++;
        }
    }

    private String containerName(ApplicationEnvironment applicationEnvironment) {
        return applicationEnvironment.prefix("container");
    }

    private CfnTaskDefinition.KeyValuePairProperty keyValuePair(String key, String value) {
        return CfnTaskDefinition.KeyValuePairProperty.builder()
                .name(key)
                .value(value)
                .build();
    }

    private List<CfnTaskDefinition.KeyValuePairProperty> toKeyValuePairs(Map<String, String> map) {
        List<CfnTaskDefinition.KeyValuePairProperty> keyValuePairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            keyValuePairs.add(keyValuePair(entry.getKey(), entry.getValue()));
        }
        return keyValuePairs;
    }

    public static class DockerImageSource {
        private final String dockerRepositoryName;
        private final String dockerImageTag;

        /**
         * Loads a Docker image from the given ECR repository and image tag.
         */
        public DockerImageSource(String dockerRepositoryName, String dockerImageTag) {
            this.dockerRepositoryName = Objects.requireNonNull(dockerRepositoryName);
            this.dockerImageTag = Objects.requireNonNull(dockerImageTag);
        }

        private String getDockerRepositoryName() {
            return dockerRepositoryName;
        }

        private String getDockerImageTag() {
            return dockerImageTag;
        }
    }

    public static class ServiceInputParameters {
        private static final int PARAMETER_HEALTH_CHECK_INTERVAL_SECONDS = 10;
        private static final int PARAMETER_CONTAINER_PORT = 8080;
        private static final String PARAMETER_CONTAINER_PROTOCOL = "HTTP";
        private static final int PARAMETER_HEALTH_CHECK_TIMEOUT_SECONDS = 5;
        private static final int PARAMETER_HEALTHY_THRESHOLD_COUNT = 2;
        private static final int PARAMETER_UNHEALTHY_THRESHOLD_COUNT = 8;
        private static final RetentionDays PARAMETER_RETENTION_DAYS = RetentionDays.ONE_WEEK;
        private static final int PARAMETER_CPU = 256;
        private static final int PARAMETER_MEMORY = 512;
        private static final int PARAMETER_DESIRED_INSTANCES_COUNT = 2;
        private static final int PARAMETER_MAXIMUM_INSTANCES_PERCENT = 200;
        private static final int PARAMETER_MINIMUM_HEALTHY_INSTANCES_PERCENT = 50;
        private static final boolean PARAMETER_STICKY_SESSIONS_ENABLED = false;
        private static final String PARAMETER_AWSLOGS_DATE_TIME_FORMAT = "%Y-%m-%dT%H:%M:%S.%f%z";
        private final DockerImageSource dockerImageSource;
        private final String parameterHealthCheckPath;
        private final Map<String, String> environmentVariables;
        private final List<String> securityGroupIdsToGrantIngressFromEcs;
        private final List<PolicyStatement> taskRolePolicyStatements = new ArrayList<>();

        /**
         * Knobs and dials you can configure to run a Docker image in an ECS service. The default values are set in a way
         * to work out of the box with a Spring Boot application.
         *
         * @param dockerImageSource        the source from where to load the Docker image that we want to deploy.
         * @param parameterHealthCheckPath the application's health check path
         * @param environmentVariables     the environment variables provided to the Java runtime within the Docker containers.
         */
        public ServiceInputParameters(
                DockerImageSource dockerImageSource,
                String parameterHealthCheckPath, Map<String, String> environmentVariables) {
            this.dockerImageSource = dockerImageSource;
            this.parameterHealthCheckPath = parameterHealthCheckPath;
            this.environmentVariables = environmentVariables;
            this.securityGroupIdsToGrantIngressFromEcs = Collections.emptyList();
        }
    }
}
