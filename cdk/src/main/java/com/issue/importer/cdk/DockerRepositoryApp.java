package com.issue.importer.cdk;

import com.issue.importer.cdk.construct.DockerRepository;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class DockerRepositoryApp {

    public static void main(final String[] args) {
        App app = new App();

        String accountId = Validations.requireNonEmpty(app, "accountId");
        String region = Validations.requireNonEmpty(app, "region");
        String dockerRepositoryName = Validations.requireNonEmpty(app, "dockerRepositoryName");

        Environment awsEnvironment = makeEnv(accountId, region);

        Stack dockerRepositoryStack = new Stack(app, "DockerRepositoryStack", StackProps.builder()
                .stackName(dockerRepositoryName + "-DockerRepository")
                .env(awsEnvironment)
                .build());

        new DockerRepository(
                dockerRepositoryStack,
                "DockerRepository",
                new DockerRepository.DockerRepositoryInputParameters(dockerRepositoryName, accountId));

        app.synth();
    }

    static Environment makeEnv(String account, String region) {
        return Environment.builder()
                .account(account)
                .region(region)
                .build();
    }
}
