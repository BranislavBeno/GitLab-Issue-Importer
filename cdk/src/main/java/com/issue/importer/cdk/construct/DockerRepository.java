package com.issue.importer.cdk.construct;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.ecr.IRepository;
import software.amazon.awscdk.services.ecr.LifecycleRule;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.iam.AccountPrincipal;
import software.constructs.Construct;

import java.util.Collections;
import java.util.Objects;

/**
 * Provisions an ECR repository for Docker images. Every user in the given account will have access
 * to push and pull images.
 */
public class DockerRepository extends Construct {

    public DockerRepository(
            final Construct scope,
            final String id,
            final DockerRepositoryInputParameters dockerRepositoryInputParameters) {
        super(scope, id);

        IRepository ecrRepository = Repository.Builder.create(this, "ecrRepository")
                .repositoryName(dockerRepositoryInputParameters.dockerRepositoryName)
                .removalPolicy(dockerRepositoryInputParameters.retainRegistryOnDelete ? RemovalPolicy.RETAIN : RemovalPolicy.DESTROY)
                .lifecycleRules(Collections.singletonList(LifecycleRule.builder()
                        .rulePriority(1)
                        .description("limit to " + dockerRepositoryInputParameters.maxImageCount + " images")
                        .maxImageCount(dockerRepositoryInputParameters.maxImageCount)
                        .build()))
                .build();

        // grant pull and push to all users of the account
        ecrRepository.grantPullPush(new AccountPrincipal(dockerRepositoryInputParameters.accountId));
    }

    public static class DockerRepositoryInputParameters {
        private final String dockerRepositoryName;
        private final String accountId;
        private final int maxImageCount;
        private final boolean retainRegistryOnDelete;

        /**
         * @param dockerRepositoryName the name of the docker repository to create.
         * @param accountId            ID of the AWS account which shall have permission to push and pull the Docker repository.
         */
        public DockerRepositoryInputParameters(String dockerRepositoryName, String accountId) {
            this(dockerRepositoryName, accountId, 10);
        }

        /**
         * @param dockerRepositoryName the name of the docker repository to create.
         * @param accountId            ID of the AWS account which shall have permission to push and pull the Docker repository.
         * @param maxImageCount        the maximum number of images to be held in the repository before old images get deleted.
         */
        public DockerRepositoryInputParameters(String dockerRepositoryName, String accountId, int maxImageCount) {
            this(dockerRepositoryName, accountId, maxImageCount, true);
        }

        /**
         * @param dockerRepositoryName   the name of the docker repository to create.
         * @param accountId              ID of the AWS account which shall have permission to push and pull the Docker repository.
         * @param maxImageCount          the maximum number of images to be held in the repository before old images get deleted.
         * @param retainRegistryOnDelete indicating whether the container registry should be destroyed or retained on deletion.
         */
        public DockerRepositoryInputParameters(String dockerRepositoryName, String accountId, int maxImageCount, boolean retainRegistryOnDelete) {
            this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
            this.dockerRepositoryName = Objects.requireNonNull(dockerRepositoryName, "dockerRepositoryName must not be null");
            this.maxImageCount = maxImageCount;
            this.retainRegistryOnDelete = retainRegistryOnDelete;
        }
    }
}
