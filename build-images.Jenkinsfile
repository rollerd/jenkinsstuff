import java.text.SimpleDateFormat
@Library('my') _
import com.my.utils.PodTemplates

podTemplates     = new PodTemplates()
def cluster_env  = params['ENVIRONMENT'].tokenize(',')
def partner_name = params['API_SERVICE']

if (!params.__INIT__) {
  properties([parameters([
              choice(name: 'ENVIRONMENT', choices: cluster_env, description: 'The AWS environment to build in'),
              string(name: 'GITBRANCH', defaultValue: '', description: 'Branch of API code to deploy', trim: false),
              string(name: 'API_SERVICE', defaultValue: partner_name, description: 'Partner name', trim: false),
              string(name: 'IMAGE_TAG_VERSION', defaultValue: '', description: 'Optional. Tag images with the specified string rather than the git branch (the default)', trim: false),
              booleanParam(name: 'RUN_TESTS', defaultValue: false, description: 'Run maven with tests'),
              booleanParam(name: 'SONARQUBE', defaultValue: false, description: 'Run Sonarqube scan'),
              booleanParam(name: 'SKIP_QUALITYGATE', defaultValue: false, description: 'Skip Sonarqube quality gate (will build images despite result of quality gate)'),
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
def SONARQUBE       = environment_map[params['ENVIRONMENT']][6]

def API_SERVICE    = params.API_SERVICE

def IMAGE_TAG = "${params.GITBRANCH}".replaceAll("/", ".")
if (!params.IMAGE_TAG_VERSION.isEmpty()){
  IMAGE_TAG = params.IMAGE_TAG_VERSION.replaceAll("/", ".")
}

def MAVEN_COMMAND = "mvn -T 4 clean package -Dmaven.test.skip=true"
if (params.RUN_TESTS == true) {
  MAVEN_COMMAND = "mvn -T 4 clean package"
}

// Cannot be any leading spaces before closing EOF
def create_dockerfile(jar_file,service_name) {
sh """
cat <<EOF > Dockerfile
FROM ${ACCOUNT}.dkr.ecr.${REGION}.amazonaws.com/api/openjdk:8-jre
VOLUME /tmp
WORKDIR /usr/share
COPY target/${jar_file} /usr/share/app/${service_name}.jar
EOF
"""
}

try {
  ansiColor('xterm'){
    withEnv(["ACCOUNT=${ACCOUNT}","REGION=${REGION}", "ENV=${ENVIRON}", "CLUSTER_NAME=${CLUSTER_NAME}"]){
      podTemplates.buildMaven {
        node(POD_LABEL){
          container('info'){
              podtemplate_containers = sh(script: 'echo $PODTEMPLATE_CONTAINERS', returnStdout: true).trim()
              podtemplate_info = sh(script: 'echo $PODTEMPLATE_INFO', returnStdout: true).trim()
              nodename = sh(script: 'echo $NODENAME', returnStdout: true).trim()
              cprint("GREEN", "Pod template info:\n$podtemplate_info".replaceAll(",", "\n"))
              cprint("GREEN", "Pod template containers:\n$podtemplate_containers".replaceAll(",", "\n"))
              cprint("GREEN", "Node name: $nodename")
          }
  
          def jobStageList = [
            "service-1",
            "service-2",
            "service-3",
            "service-4",
            "service-5"
          ]
  
          def parallelStagesMap = jobStageList.collectEntries {
            ["${it}" : generateStage(it,"${ACCOUNT}","${REGION}","${ECR_CREDENTIALS}","${IMAGE_TAG}")]
          }
  
          stage('Compile'){
            script{
              container('maven'){
                cprint("BLUE", "Checkout API")
                cprint("BLUE", "Using bitbucket creds id: ${BITBUCKET_CREDS}")
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: "prod-aws"]]){
                  dir('api'){
                    checkout([$class: 'GitSCM', 
                              branches: [[name: params.GITBRANCH ]],
                              userRemoteConfigs: [[credentialsId: BITBUCKET_CREDS, url: 'git@bitbucket.org:my/api.git']]
                              ])
                    env.artifactVersion = readMavenPom().getVersion()
                    cprint("BLUE", "Compiling api")
                    sh "${MAVEN_COMMAND}"
                    cprint("BLUE", "Artifact version: ${artifactVersion}")
                    cprint("GREEN", "Compilation complete")
                  }
                }
              }
            }
          }
  
          stage('Sonarqube'){
            if (params.SONARQUBE == true){
              container('maven11'){
                dir('api'){
                  cprint("BLUE", "Running sonarqube scan")
                  withSonarQubeEnv("${SONARQUBE}") {
                    sh "mvn sonar:sonar -Dmaven.test.skip=true"
                    cprint("GREEN", "Sonarqube scan complete")
                  }
                }
              } 
            }else{
              cprint("BLUE", "Skipping sonarqube scan")
            }
          }
  
          stage("Quality gate") {
            if (params.SONARQUBE == true){
              if (params.SKIP_QUALITYGATE != true){
                cprint("BLUE", "Awaiting sonarqube quality gate webhook response")
                timeout(time: 5, unit: 'MINUTES') {
                  def qg = waitForQualityGate()
                  if (qg.status != 'OK') {
                    error cprint("RED", "Pipeline aborted due to quality gate failure: ${qg.status}")
                  }else{
                    cprint("GREEN", "Sonarqube quality gate passed!")
                  }
                }
              }else{
                cprint("BLUE", "Skipping quality gate check")
              }
            }
          }
  
          stage('Build Images'){
            container('docker'){
              parallelStagesMap.failFast = true
              parallel parallelStagesMap
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

def generateStage(service,account,region,credentials,image_tag) {
  return {
    stage("Parallel: ${service}"){
      container("docker"){
        dir("api/${service}"){
          cprint("BLUE", "Building image for: '${service}' with API_SERVICE: ${params.API_SERVICE}")
          create_dockerfile("${service}-${env.artifactVersion}.jar","${service}")
          docker.withRegistry("https://${account}.dkr.ecr.${region}.amazonaws.com", "${credentials}"){
            def image =  docker.build("${account}.dkr.ecr.${region}.amazonaws.com/api/${service}:${params.API_SERVICE}", "--pull -f Dockerfile .")
            image.push()
            image.push("${params.API_SERVICE}-${image_tag}")
            cprint("GREEN", "Successfully pushed ${service}:${params.API_SERVICE}-${image_tag}")
          }
        }
      }
    }
  }
}
