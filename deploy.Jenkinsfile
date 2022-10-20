import java.text.SimpleDateFormat
@Library('my') _
import com.my.utils.PodTemplates

podTemplates        = new PodTemplates()
def cluster_env     = params['ENVIRONMENT'].tokenize(',')
def api_version    = params['API_SERVICE']
def backend_profile = params['BACKEND_PROFILE']

if (!params.__INIT__) {
  properties([parameters([
              choice(name: 'ENVIRONMENT', choices: cluster_env, description: 'The AWS environment to build in'),
              string(name: 'GITBRANCH', defaultValue: '', description: 'Branch of API code to deploy', trim: false),
              string(name: 'BACKEND_PROFILE', defaultValue: backend_profile, description: 'API Spring profile', trim: false),
              string(name: 'API_SERVICE', defaultValue: api_version, description: 'Partner name', trim: false),
              string(name: 'CHART_VERSION', defaultValue: "latest", description: 'Helm chart version to deploy', trim: false),
              string(name: 'GATEWAY_PARTNER', defaultValue: "", description: 'Optional. If set, will deploy internal ingress with the named gateway partner', trim: false),
              string(name: 'GATEWAY_DNS', defaultValue: "", description: 'Optional. If using api-gateway, set to api-gateway DNS record', trim: false),
              string(name: 'IMAGE_TAG_VERSION', defaultValue: '', description: 'Optional. Tag images with the specified string rather than the git branch (the default)', trim: false),
              booleanParam(name: 'PUBLISH_DOCS', defaultValue: false, description: "Publish the Swagger docs to ingress"),
              booleanParam(name: '__INIT__', defaultValue: true, description: "WARNING: Uncheck this field and run this job once if you want to re init parameter."),
  ]) ])
  return
}

environment_map = get_environment_map()
// Set vars based on ENVIRONMENT param chosen by user in GUI
def ACCOUNT         = environment_map[params['ENVIRONMENT']][0]
def REGION          = environment_map[params['ENVIRONMENT']][1]
def ENVIRON         = environment_map[params['ENVIRONMENT']][2]
def ECR_CREDENTIALS = environment_map[params['ENVIRONMENT']][3]
def BITBUCKET_CREDS = environment_map[params['ENVIRONMENT']][4]
def CLUSTER_NAME    = environment_map[params['ENVIRONMENT']][5]

// Replaces any '/' with a '.' in the image tag, since '/' is invalid for a tag
// Optionally overrides the gitbranch tag with the IMAGE_TAG_VERSION if specified
def IMAGE_TAG = "${params.GITBRANCH}".replaceAll("/", ".")
if (!params.IMAGE_TAG_VERSION.isEmpty()){
  IMAGE_TAG = params.IMAGE_TAG_VERSION.replaceAll("/", ".")
}

// Helm chart version to use in the deploy. Defaults to latest
def CHART_VERSION = ""
if (params.CHART_VERSION != "latest"){
    CHART_VERSION = "--version ${params.CHART_VERSION}"
}

// Publish the api swagger docs if set
def publishDocs = false
if (params.PUBLISH_DOCS == true) {
    publishDocs = true
}

// Deploy service1 ingress with the specified gateway. Otherwise, it will be skipped
def deployGatewayIngress = false
if (params.GATEWAY_PARTNER != "" && params.GATEWAY_PARTNER != null && params.GATEWAY_PARTNER != "null") {
    deployGatewayIngress = true
}

try {
  ansiColor('xterm'){
    withEnv(["ACCOUNT=${ACCOUNT}","REGION=${REGION}", "AWS_DEFAULT_REGION=${REGION}", "CLUSTER_NAME=${CLUSTER_NAME}"]){
      podTemplates.deployTemplate {
        node(POD_LABEL){
          // Print info about the pod template and build environment
          container('info'){
              podtemplate_containers = sh(script: 'echo $PODTEMPLATE_CONTAINERS', returnStdout: true).trim()
              podtemplate_info = sh(script: 'echo $PODTEMPLATE_INFO', returnStdout: true).trim()
              nodename = sh(script: 'echo $NODENAME', returnStdout: true).trim()
              cprint("GREEN", "Pod template info:\n$podtemplate_info".replaceAll(",", "\n"))
              cprint("GREEN", "Pod template containers:\n$podtemplate_containers".replaceAll(",", "\n"))
              cprint("GREEN", "Node name: $nodename")
          }
          stage('Helm Deploy'){
            container('helm'){

              // Pull config repo and render any secrets
              dir('helm_config'){
                cprint("BLUE", "Pulling helm_config values")
                git branch: 'master', url: 'git@bitbucket.org:my/helm_config.git', credentialsId: "${BITBUCKET_CREDS}"
                cprint("BLUE", "Rendering secrets")
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: "${ENVIRON}-aws"]]){
                  sh "secret_replace.py -f api/values/${ENVIRON}/${CLUSTER_NAME}/secrets.yaml"
                }
              }

              // Pull and update helm chart repo 
              withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: "${ENVIRON}-aws"]]){
                cprint("BLUE", "Adding required Helm repos")
                sh "helm repo add helm-charts s3://my-helm-charts/charts"
                cprint("BLUE", "Updating Helm repos")
                sh "helm repo update"
              }

              // Deploy helm chart
              def parallelSteps = [
                "helmDeploy": getHelmDeployStage(CHART_VERSION,params.API_SERVICE,env.WORKSPACE,ENVIRON,CLUSTER_NAME,params.BACKEND_PROFILE,IMAGE_TAG,publishDocs,deployGatewayIngress,params.GATEWAY_PARTNER,params.GATEWAY_DNS),
                "podCheck": getPodCheckStage(params.API_SERVICE)
              ] 

              parallel parallelSteps
            }
          }
        }
      }
    }
  }
}catch (e) {
  throw e
}finally {
  manager.addShortText("${IMAGE_TAG}")
}

// Prints out pod list for troubleshooting purposes if deploy fails
def getPodCheckStage(api_version) {
  return {
    stage("Parallel: CheckPods"){
      container("helm"){
        sh """
        sleep 30
        kubectl get pods -n $api_version
        sleep 30
        kubectl get pods -n $api_version
        """
      }
    }
  }
}

// Run the helm deploy
def getHelmDeployStage(chart_version,api_version,workspace,environ,cluster_name,backend_profile,image_tag,publish_docs,deploy_gateway_ingress,gateway_service_name,api_gateway_dns) {
  return {
    stage("Parallel: Helm deploy"){
      try {
        dir('api'){
          cprint("BLUE", "Running helm install")
          sh """
              helm upgrade --wait --install $chart_version --create-namespace --namespace $api_version  $api_version-api helm-charts/api \
              -f $workspace/helm_config/api/values/$environ/$cluster_name/values.yaml \
              -f $workspace/helm_config/api/values/$environ/$cluster_name/secrets.yaml \
              --set profile=$backend_profile \
              --set releaseTag=$image_tag \
              --set apiVersion=$api_version \
              --set docs.publishDocs=$publish_docs \
              --set service1.gatewayIngress.deploy=$deploy_gateway_ingress \
              --set service1.gatewayIngress.serviceName=$gateway_service_name \
              --set apiGatewayDns=$api_gateway_dns
             """
        }
      }catch (e) {
        if (e.getClass() == org.jenkinsci.plugins.workflow.steps.FlowInterruptedException) {
          cprint("MAGENTA", "Caught abort before Helm deploy was complete. Rolling back deployment!")
          sleep(5)
          sh "helm rollback $api_version-api -n $api_version"
        }
        throw e
      }
    }
  }
}
