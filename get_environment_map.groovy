def call() {
  def environment_map = [:]
  environment_map.put("dev-west",     ["<ACCOUNT>",
                                       "us-west-2",
                                       "dev",
                                       "ecr:us-west-2:dev-aws",
                                       "dev-bitbucket-ssh",
                                       "<CLUSTERNAME>",
                                       "sonarqube-dev"])
  environment_map.put("dev-west-B",   ["<ACCOUNT>",
                                       "us-west-2",
                                       "dev",
                                       "ecr:us-west-2:dev-aws",
                                       "dev-bitbucket-ssh",
                                       "<CLUSTERNAME>",
                                       "sonarqube-dev"])
  environment_map.put("staging-west", ["<ACCOUNT>",
                                       "us-west-2",
                                       "staging",
                                       "ecr:us-west-2:staging-aws",
                                       "staging-bitbucket-ssh",
                                       "<CLUSTERNAME>",
                                       "sonarqube"])
  environment_map.put("prod-west",    ["<ACCOUNT>",
                                       "us-west-2",
                                       "prod",
                                       "ecr:us-west-2:prod-aws",
                                       "prod-bitbucket-ssh",
                                       "<CLUSTERNAME>",
                                       "sonarqube"])
  return environment_map
}

// map values:
// [0] - AWS account number
// [1] - AWS region
// [2] - Environment
// [3] - AWS ECR Jenkins credentials name
// [4] - Jenkins bitbucket SSH creds
// [5] - Cluster name
// [6] - sonarqube config name
