package com.issue.importer.cdk.construct;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Tags;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ICluster;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Creates a base network for an application served by ECS. The network stack contains a VPC,
 * two public and two isolated subnets, an ECS cluster, and an internet-facing load balancer with an HTTP and
 * an optional HTTPS listener. The listeners can be used in other stacks to attach to an ECS service,
 * for instance.
 * <p>
 * The construct exposes some output parameters to be used by other constructs. You can access them by:
 * <ul>
 *     <li>calling {@link #getOutputParameters()} to load the output parameters from a {@link Network} instance</li>
 *     <li>calling the static method {@link #getOutputParametersFromParameterStore(Construct, ApplicationEnvironment)} to load them from the
 *     parameter store (requires that a {@link Network} construct has already been provisioned in a previous stack) </li>
 * </ul>
 */
public class Network extends Construct {

    private static final String PARAMETER_VPC_ID = "vpcId";
    private static final String PARAMETER_HTTP_LISTENER_ARN = "httpListenerArn";
    private static final String PARAMETER_HTTPS_LISTENER_ARN = "httpsListenerArn";
    private static final String PARAMETER_LOAD_BALANCER_SECURITY_GROUP_ID = "loadBalancerSecurityGroupId";
    private static final String PARAMETER_ECS_CLUSTER_NAME = "ecsClusterName";
    private static final String PARAMETER_ISOLATED_SUBNET_ONE = "isolatedSubnetIdOne";
    private static final String PARAMETER_ISOLATED_SUBNET_TWO = "isolatedSubnetIdTwo";
    private static final String PARAMETER_PUBLIC_SUBNET_ONE = "publicSubnetIdOne";
    private static final String PARAMETER_PUBLIC_SUBNET_TWO = "publicSubnetIdTwo";
    private static final String PARAMETER_AVAILABILITY_ZONE_ONE = "availabilityZoneOne";
    private static final String PARAMETER_AVAILABILITY_ZONE_TWO = "availabilityZoneTwo";
    private static final String PARAMETER_LOAD_BALANCER_ARN = "loadBalancerArn";
    private static final String PARAMETER_LOAD_BALANCER_DNS_NAME = "loadBalancerDnsName";
    private static final String PARAMETER_LOAD_BALANCER_HOSTED_ZONE_ID = "loadBalancerCanonicalHostedZoneId";
    private static final String PARAMETER_HTTPS_LISTENER = "httpsListener";
    private static final String PARAMETER_HTTP_LISTENER = "httpListener";
    private final IVpc vpc;
    private final ApplicationEnvironment appEnvironment;
    private final ICluster ecsCluster;
    private IApplicationListener httpListener;
    private IApplicationListener httpsListener;
    private ISecurityGroup loadBalancerSecurityGroup;
    private IApplicationLoadBalancer loadBalancer;

    public Network(
            final Construct scope,
            final String id,
            final ApplicationEnvironment appEnvironment,
            final NetworkInputParameters networkInputParameters) {

        super(scope, id);

        this.appEnvironment = appEnvironment;

        this.vpc = createVpc();

        // We're preparing an ECS cluster in the network stack and using it in the ECS stack.
        // If the cluster were in the ECS stack, it would interfere with deleting the ECS stack,
        // because an ECS service would still depend on it.
        this.ecsCluster = Cluster.Builder.create(this, "cluster")
                .vpc(this.vpc)
                .clusterName(prefixWithUniqueName("ecsCluster"))
                .build();

        createLoadBalancer(vpc, networkInputParameters.getSslCertificateArn());

        Tags.of(this).add("environment", appEnvironment.environmentName());
    }

    /**
     * Collects the output parameters of an already deployed {@link Network} construct from the parameter store. This requires
     * that a {@link Network} construct has been deployed previously. If you want to access the parameters from the same
     * stack that the {@link Network} construct is in, use the plain {@link #getOutputParameters()} method.
     *
     * @param scope          the construct in which we need the output parameters
     * @param appEnvironment the name of the application and its environment for which to load the output parameters. The deployed {@link Network}
     *                       construct must have been deployed into this environment.
     */
    public static NetworkOutputParameters getOutputParametersFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {
        return new NetworkOutputParameters(
                getVpcIdFromParameterStore(scope, appEnvironment),
                getHttpListenerArnFromParameterStore(scope, appEnvironment),
                getHttpsListenerArnFromParameterStore(scope, appEnvironment),
                getLoadBalancerSecurityGroupIdFromParameterStore(scope, appEnvironment),
                getEcsClusterNameFromParameterStore(scope, appEnvironment),
                getIsolatedSubnetsFromParameterStore(scope, appEnvironment),
                getPublicSubnetsFromParameterStore(scope, appEnvironment),
                getAvailabilityZonesFromParameterStore(scope, appEnvironment),
                getLoadBalancerArnFromParameterStore(scope, appEnvironment),
                getLoadBalancerDnsNameFromParameterStore(scope, appEnvironment),
                getLoadBalancerCanonicalHostedZoneIdFromParameterStore(scope, appEnvironment)
        );
    }

    @NotNull
    private static String createParameterName(ApplicationEnvironment appEnvironment, String parameterName) {
        return "%s-%s-Network-%s".formatted(appEnvironment.environmentName(), appEnvironment.applicationName(), parameterName);
    }

    private static String getVpcIdFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {
        return StringParameter.fromStringParameterName(scope, PARAMETER_VPC_ID, createParameterName(appEnvironment, PARAMETER_VPC_ID))
                .getStringValue();
    }

    private static String getHttpListenerArnFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {
        return StringParameter.fromStringParameterName(scope, PARAMETER_HTTP_LISTENER_ARN, createParameterName(appEnvironment, PARAMETER_HTTP_LISTENER_ARN))
                .getStringValue();
    }

    private static String getHttpsListenerArnFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {
        String value = StringParameter.fromStringParameterName(scope, PARAMETER_HTTPS_LISTENER_ARN, createParameterName(appEnvironment, PARAMETER_HTTPS_LISTENER_ARN))
                .getStringValue();
        if ("null".equals(value)) {
            return null;
        } else {
            return value;
        }
    }

    private static String getLoadBalancerSecurityGroupIdFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {
        return StringParameter.fromStringParameterName(scope, PARAMETER_LOAD_BALANCER_SECURITY_GROUP_ID, createParameterName(appEnvironment, PARAMETER_LOAD_BALANCER_SECURITY_GROUP_ID))
                .getStringValue();
    }

    private static String getEcsClusterNameFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {
        return StringParameter.fromStringParameterName(scope, PARAMETER_ECS_CLUSTER_NAME, createParameterName(appEnvironment, PARAMETER_ECS_CLUSTER_NAME))
                .getStringValue();
    }

    private static List<String> getIsolatedSubnetsFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {

        String subnetOneId = StringParameter.fromStringParameterName(scope, PARAMETER_ISOLATED_SUBNET_ONE, createParameterName(appEnvironment, PARAMETER_ISOLATED_SUBNET_ONE))
                .getStringValue();

        String subnetTwoId = StringParameter.fromStringParameterName(scope, PARAMETER_ISOLATED_SUBNET_TWO, createParameterName(appEnvironment, PARAMETER_ISOLATED_SUBNET_TWO))
                .getStringValue();

        return asList(subnetOneId, subnetTwoId);
    }

    private static List<String> getPublicSubnetsFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {

        String subnetOneId = StringParameter.fromStringParameterName(scope, PARAMETER_PUBLIC_SUBNET_ONE, createParameterName(appEnvironment, PARAMETER_PUBLIC_SUBNET_ONE))
                .getStringValue();

        String subnetTwoId = StringParameter.fromStringParameterName(scope, PARAMETER_PUBLIC_SUBNET_TWO, createParameterName(appEnvironment, PARAMETER_PUBLIC_SUBNET_TWO))
                .getStringValue();

        return asList(subnetOneId, subnetTwoId);
    }

    private static List<String> getAvailabilityZonesFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {

        String availabilityZoneOne = StringParameter.fromStringParameterName(scope, PARAMETER_AVAILABILITY_ZONE_ONE, createParameterName(appEnvironment, PARAMETER_AVAILABILITY_ZONE_ONE))
                .getStringValue();

        String availabilityZoneTwo = StringParameter.fromStringParameterName(scope, PARAMETER_AVAILABILITY_ZONE_TWO, createParameterName(appEnvironment, PARAMETER_AVAILABILITY_ZONE_TWO))
                .getStringValue();

        return asList(availabilityZoneOne, availabilityZoneTwo);
    }

    private static String getLoadBalancerArnFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {
        return StringParameter.fromStringParameterName(scope, PARAMETER_LOAD_BALANCER_ARN, createParameterName(appEnvironment, PARAMETER_LOAD_BALANCER_ARN))
                .getStringValue();
    }

    private static String getLoadBalancerDnsNameFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {
        return StringParameter.fromStringParameterName(scope, PARAMETER_LOAD_BALANCER_DNS_NAME, createParameterName(appEnvironment, PARAMETER_LOAD_BALANCER_DNS_NAME))
                .getStringValue();
    }

    private static String getLoadBalancerCanonicalHostedZoneIdFromParameterStore(Construct scope, ApplicationEnvironment appEnvironment) {
        return StringParameter.fromStringParameterName(scope, PARAMETER_LOAD_BALANCER_HOSTED_ZONE_ID, createParameterName(appEnvironment, PARAMETER_LOAD_BALANCER_HOSTED_ZONE_ID))
                .getStringValue();
    }

    /**
     * Creates a VPC with 2 private and 2 public subnets in different AZs and without a NAT gateway
     * (i.e. the private subnets have no access to the internet).
     */
    private IVpc createVpc() {

        SubnetConfiguration publicSubnets = SubnetConfiguration.builder()
                .subnetType(SubnetType.PUBLIC)
                .name(prefixWithUniqueName("publicSubnet"))
                .build();

        SubnetConfiguration isolatedSubnets = SubnetConfiguration.builder()
                .subnetType(SubnetType.PRIVATE_ISOLATED)
                .name(prefixWithUniqueName("isolatedSubnet"))
                .build();

        return Vpc.Builder.create(this, "vpc")
                .natGateways(0)
                .maxAzs(2)
                .subnetConfiguration(asList(
                        publicSubnets,
                        isolatedSubnets
                ))
                .build();
    }

    private String prefixWithUniqueName(String string) {
        return "%s-%s-%s".formatted(appEnvironment.environmentName(), appEnvironment.applicationName(), string);
    }

    /**
     * Creates a load balancer that accepts HTTP and HTTPS requests from the internet and puts it into
     * the VPC's public subnets.
     */
    private void createLoadBalancer(
            final IVpc vpc,
            final String sslCertificateArn) {

        loadBalancerSecurityGroup = SecurityGroup.Builder.create(this, "loadBalancerSecurityGroup")
                .securityGroupName(prefixWithUniqueName("loadBalancerSecurityGroup"))
                .description("Public access to the load balancer.")
                .vpc(vpc)
                .build();

        CfnSecurityGroupIngress.Builder.create(this, "ingressToLoadBalancer")
                .groupId(loadBalancerSecurityGroup.getSecurityGroupId())
                .cidrIp("0.0.0.0/0")
                .ipProtocol("-1")
                .build();

        loadBalancer = ApplicationLoadBalancer.Builder.create(this, "loadBalancer")
                .loadBalancerName(prefixWithUniqueName("loadBalancer"))
                .vpc(vpc)
                .internetFacing(true)
                .securityGroup(loadBalancerSecurityGroup)
                .build();

        IApplicationTargetGroup dummyTargetGroup = ApplicationTargetGroup.Builder.create(this, "defaultTargetGroup")
                .vpc(vpc)
                .port(8080)
                .protocol(ApplicationProtocol.HTTP)
                .targetGroupName(prefixWithUniqueName("no-op-targetGroup"))
                .targetType(TargetType.IP)
                .deregistrationDelay(Duration.seconds(5))
                .healthCheck(
                        HealthCheck
                                .builder()
                                .healthyThresholdCount(2)
                                .interval(Duration.seconds(10))
                                .timeout(Duration.seconds(5))
                                .build()
                )
                .build();

        httpListener = loadBalancer.addListener(PARAMETER_HTTP_LISTENER, BaseApplicationListenerProps.builder()
                .port(80)
                .protocol(ApplicationProtocol.HTTP)
                .open(true)
                .build());

        httpListener.addTargetGroups("http-defaultTargetGroup", AddApplicationTargetGroupsProps.builder()
                .targetGroups(Collections.singletonList(dummyTargetGroup))
                .build());

        if (sslCertificateArn != null) {
            IListenerCertificate certificate = ListenerCertificate.fromArn(sslCertificateArn);
            httpsListener = loadBalancer.addListener(PARAMETER_HTTPS_LISTENER, BaseApplicationListenerProps.builder()
                    .port(443)
                    .protocol(ApplicationProtocol.HTTPS)
                    .certificates(Collections.singletonList(certificate))
                    .open(true)
                    .build());

            httpsListener.addTargetGroups("https-defaultTargetGroup", AddApplicationTargetGroupsProps.builder()
                    .targetGroups(Collections.singletonList(dummyTargetGroup))
                    .build());

            ListenerAction redirectAction = ListenerAction.redirect(
                    RedirectOptions.builder()
                            .protocol("HTTPS")
                            .port("443")
                            .build()
            );

            new ApplicationListenerRule(
                    this,
                    "HttpListenerRule",
                    ApplicationListenerRuleProps.builder()
                            .listener(httpListener)
                            .priority(1)
                            .conditions(List.of(ListenerCondition.pathPatterns(List.of("*"))))
                            .action(redirectAction)
                            .build()
            );
        }

        createOutputParameters();
    }

    /**
     * Stores output parameters of this stack in the parameter store, so they can be retrieved by other stacks
     * or constructs as necessary.
     */
    private void createOutputParameters() {

        StringParameter.Builder.create(this, PARAMETER_VPC_ID)
                .parameterName(createParameterName(appEnvironment, PARAMETER_VPC_ID))
                .stringValue(this.vpc.getVpcId())
                .build();

        StringParameter.Builder.create(this, PARAMETER_HTTP_LISTENER)
                .parameterName(createParameterName(appEnvironment, PARAMETER_HTTP_LISTENER_ARN))
                .stringValue(this.httpListener.getListenerArn())
                .build();

        if (this.httpsListener != null) {
            StringParameter.Builder.create(this, PARAMETER_HTTPS_LISTENER)
                    .parameterName(createParameterName(appEnvironment, PARAMETER_HTTPS_LISTENER_ARN))
                    .stringValue(this.httpsListener.getListenerArn())
                    .build();
        } else {
            StringParameter.Builder.create(this, PARAMETER_HTTPS_LISTENER)
                    .parameterName(createParameterName(appEnvironment, PARAMETER_HTTPS_LISTENER_ARN))
                    .stringValue("null")
                    .build();
        }

        StringParameter.Builder.create(this, PARAMETER_LOAD_BALANCER_SECURITY_GROUP_ID)
                .parameterName(createParameterName(appEnvironment, PARAMETER_LOAD_BALANCER_SECURITY_GROUP_ID))
                .stringValue(this.loadBalancerSecurityGroup.getSecurityGroupId())
                .build();

        StringParameter.Builder.create(this, PARAMETER_ECS_CLUSTER_NAME)
                .parameterName(createParameterName(appEnvironment, PARAMETER_ECS_CLUSTER_NAME))
                .stringValue(this.ecsCluster.getClusterName())
                .build();

        // I would have liked to use StringListParameter to store a list of AZs, but it's currently broken (https://github.com/aws/aws-cdk/issues/3586).
        StringParameter.Builder.create(this, PARAMETER_AVAILABILITY_ZONE_ONE)
                .parameterName(createParameterName(appEnvironment, PARAMETER_AVAILABILITY_ZONE_ONE))
                .stringValue(vpc.getAvailabilityZones().get(0))
                .build();

        StringParameter.Builder.create(this, PARAMETER_AVAILABILITY_ZONE_TWO)
                .parameterName(createParameterName(appEnvironment, PARAMETER_AVAILABILITY_ZONE_TWO))
                .stringValue(vpc.getAvailabilityZones().get(1))
                .build();

        // I would have liked to use StringListParameter to store a list of AZs, but it's currently broken (https://github.com/aws/aws-cdk/issues/3586).
        StringParameter.Builder.create(this, "isolatedSubnetOne")
                .parameterName(createParameterName(appEnvironment, PARAMETER_ISOLATED_SUBNET_ONE))
                .stringValue(this.vpc.getIsolatedSubnets().get(0).getSubnetId())
                .build();

        StringParameter.Builder.create(this, "isolatedSubnetTwo")
                .parameterName(createParameterName(appEnvironment, PARAMETER_ISOLATED_SUBNET_TWO))
                .stringValue(this.vpc.getIsolatedSubnets().get(1).getSubnetId())
                .build();

        // I would have liked to use StringListParameter to store a list of AZs, but it's currently broken (https://github.com/aws/aws-cdk/issues/3586).
        StringParameter.Builder.create(this, "publicSubnetOne")
                .parameterName(createParameterName(appEnvironment, PARAMETER_PUBLIC_SUBNET_ONE))
                .stringValue(this.vpc.getPublicSubnets().get(0).getSubnetId())
                .build();

        StringParameter.Builder.create(this, "publicSubnetTwo")
                .parameterName(createParameterName(appEnvironment, PARAMETER_PUBLIC_SUBNET_TWO))
                .stringValue(this.vpc.getPublicSubnets().get(1).getSubnetId())
                .build();

        StringParameter.Builder.create(this, PARAMETER_LOAD_BALANCER_ARN)
                .parameterName(createParameterName(appEnvironment, PARAMETER_LOAD_BALANCER_ARN))
                .stringValue(this.loadBalancer.getLoadBalancerArn())
                .build();

        StringParameter.Builder.create(this, PARAMETER_LOAD_BALANCER_DNS_NAME)
                .parameterName(createParameterName(appEnvironment, PARAMETER_LOAD_BALANCER_DNS_NAME))
                .stringValue(this.loadBalancer.getLoadBalancerDnsName())
                .build();

        StringParameter.Builder.create(this, PARAMETER_LOAD_BALANCER_HOSTED_ZONE_ID)
                .parameterName(createParameterName(appEnvironment, PARAMETER_LOAD_BALANCER_HOSTED_ZONE_ID))
                .stringValue(this.loadBalancer.getLoadBalancerCanonicalHostedZoneId())
                .build();
    }

    /**
     * Collects the output parameters of this construct that might be of interest to other constructs.
     */
    public NetworkOutputParameters getOutputParameters() {
        return new NetworkOutputParameters(
                this.vpc.getVpcId(),
                this.httpListener.getListenerArn(),
                this.httpsListener != null ? this.httpsListener.getListenerArn() : null,
                this.loadBalancerSecurityGroup.getSecurityGroupId(),
                this.ecsCluster.getClusterName(),
                this.vpc.getIsolatedSubnets().stream().map(ISubnet::getSubnetId).collect(Collectors.toList()),
                this.vpc.getPublicSubnets().stream().map(ISubnet::getSubnetId).collect(Collectors.toList()),
                this.vpc.getAvailabilityZones(),
                this.loadBalancer.getLoadBalancerArn(),
                this.loadBalancer.getLoadBalancerDnsName(),
                this.loadBalancer.getLoadBalancerCanonicalHostedZoneId()
        );
    }

    public static class NetworkInputParameters {
        private final String sslCertificateArn;

        public NetworkInputParameters() {
            this.sslCertificateArn = null;
        }

        public String getSslCertificateArn() {
            return sslCertificateArn;
        }
    }

    public static class NetworkOutputParameters {

        private final String vpcId;
        private final String httpListenerArn;
        private final String httpsListenerArn;
        private final String loadBalancerSecurityGroupId;
        private final String ecsClusterName;
        private final List<String> isolatedSubnets;
        private final List<String> publicSubnets;
        private final List<String> availabilityZones;
        private final String loadBalancerArn;
        private final String loadBalancerDnsName;
        private final String loadBalancerCanonicalHostedZoneId;

        private NetworkOutputParameters(
                String vpcId,
                String httpListenerArn,
                String httpsListenerArn,
                String loadBalancerSecurityGroupId,
                String ecsClusterName,
                List<String> isolatedSubnets,
                List<String> publicSubnets,
                List<String> availabilityZones,
                String loadBalancerArn,
                String loadBalancerDnsName,
                String loadBalancerCanonicalHostedZoneId
        ) {
            this.vpcId = vpcId;
            this.httpListenerArn = httpListenerArn;
            this.httpsListenerArn = httpsListenerArn;
            this.loadBalancerSecurityGroupId = loadBalancerSecurityGroupId;
            this.ecsClusterName = ecsClusterName;
            this.isolatedSubnets = isolatedSubnets;
            this.publicSubnets = publicSubnets;
            this.availabilityZones = availabilityZones;
            this.loadBalancerArn = loadBalancerArn;
            this.loadBalancerDnsName = loadBalancerDnsName;
            this.loadBalancerCanonicalHostedZoneId = loadBalancerCanonicalHostedZoneId;
        }

        /**
         * The VPC ID.
         */
        public String getVpcId() {
            return this.vpcId;
        }

        /**
         * The ARN of the HTTP listener.
         */
        public String getHttpListenerArn() {
            return this.httpListenerArn;
        }

        /**
         * The ARN of the HTTPS listener.
         */
        public Optional<String> getHttpsListenerArn() {
            return Optional.ofNullable(this.httpsListenerArn);
        }

        /**
         * The ID of the load balancer's security group.
         */
        public String getLoadBalancerSecurityGroupId() {
            return this.loadBalancerSecurityGroupId;
        }

        /**
         * The name of the ECS cluster.
         */
        public String getEcsClusterName() {
            return this.ecsClusterName;
        }

        /**
         * The IDs of the isolated subnets.
         */
        public List<String> getIsolatedSubnets() {
            return this.isolatedSubnets;
        }

        /**
         * The IDs of the public subnets.
         */
        public List<String> getPublicSubnets() {
            return this.publicSubnets;
        }

        /**
         * The names of the availability zones of the VPC.
         */
        public List<String> getAvailabilityZones() {
            return this.availabilityZones;
        }

        /**
         * The ARN of the load balancer.
         */
        public String getLoadBalancerArn() {
            return this.loadBalancerArn;
        }

        /**
         * The DNS name of the load balancer.
         */
        public String getLoadBalancerDnsName() {
            return loadBalancerDnsName;
        }

        /**
         * The hosted zone ID of the load balancer.
         */
        public String getLoadBalancerCanonicalHostedZoneId() {
            return loadBalancerCanonicalHostedZoneId;
        }
    }
}
