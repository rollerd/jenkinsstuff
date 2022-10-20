package com.my.utils

// Uses the s3 copy and init containers to pull node/maven data vs PVCs
public void buildMaven(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: buildMaven,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "init-s3-pull: jenkins/awscli:latest,docker: jenkins/docker:19.03.14,maven: jenkins/maven:3.6.3-jdk-8-slim,maven11: 3.6.3-jdk-11-slim,maven17: 3.8.5-openjdk-17"
              spec:
                initContainers:
                - name: init-s3-pull
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/awscli:latest
                  command: ["/bin/bash"]
                  args:
                  - -c
                  - >-
                      cd /root/.m2 &&
                      aws s3 cp s3://my-maven-repo-${ENV}/maven.tar.gz . &&
                      tar -xf maven.tar.gz
                  volumeMounts:
                  - name: my-s3-maven-repo
                    mountPath: /root/.m2
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: docker
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/docker:19.03.14
                  imagePullPolicy: Always
                  resources:
                    requests:
                      ephemeral-storage: "3Gi"
                  command:
                  - sleep
                  args:
                  - "6000"
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                  - name: my-s3-maven-repo
                    mountPath: /root/.m2
                - name: maven
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/maven:3.6.3-jdk-8-slim
                  imagePullPolicy: Always
                  resources:
                    requests:
                      ephemeral-storage: "3Gi"
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  - name: AWS
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                  - name: my-s3-maven-repo
                    mountPath: /root/.m2
                - name: maven11
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/maven11:3.6.3-jdk-11-slim
                  imagePullPolicy: Always
                  resources:
                    requests:
                      ephemeral-storage: "3Gi"
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                  - name: my-s3-maven-repo
                    mountPath: /root/.m2
                - name: maven17
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/maven17:17.0.2-jdk-slim
                  imagePullPolicy: Always
                  resources:
                    requests:
                      ephemeral-storage: "3Gi"
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                  - name: my-s3-maven-repo
                    mountPath: /root/.m2
                serviceAccountName: "jenkins"
                volumes:
                - name: dockersock
                  hostPath:
                    path: /var/run/docker.sock
                - name: my-s3-maven-repo
                  emtpyDir: {}
"""){
		body.call()
	}
}

public void buildNodeJs(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: buildNodeJs,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "init-s3-pull: jenkins/awscli:latest,docker: jenkins/docker:19.03.14,nodejs: jenkins/nodejs:8.11.3"
              spec:
                initContainers:
                - name: init-s3-pull
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/awscli:latest
                  command: ["/bin/bash"]
                  args:
                  - -c
                  - >-
                      cd /root/.npm &&
                      aws s3 cp s3://my-node-repo-${ENV}/node.tar.gz . &&
                      tar -xf node.tar.gz
                  volumeMounts:
                  - name: my-s3-node-repo
                    mountPath: /root/.npm
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: nodejs
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/nodejs:8.11.3
                  imagePullPolicy: Always
                  resources:
                    requests:
                      ephemeral-storage: "3Gi"
                  command:
                  - sleep
                  args:
                  - "6000"
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                  - name: my-s3-node-repo
                    mountPath: /root/.npm
                - name: docker
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/docker:19.03.14
                  imagePullPolicy: Always
                  resources:
                    requests:
                      ephemeral-storage: "3Gi"
                  command:
                  - sleep
                  args:
                  - "6000"
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                serviceAccountName: "jenkins"
                volumes:
                - name: dockersock
                  hostPath:
                    path: /var/run/docker.sock
                - name: my-s3-node-repo
                  emtpyDir: {}
"""){
		body.call()
	}
}

public void buildNodeJs12(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: buildNodeJs,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "init-s3-pull: jenkins/awscli:latest,docker: jenkins/docker:19.03.14,nodejs: jenkins/nodejs:12.18.2"
              spec:
                initContainers:
                - name: init-s3-pull
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/awscli:latest
                  command: ["/bin/bash"]
                  args:
                  - -c
                  - >-
                      cd /root/.npm &&
                      aws s3 cp s3://my-node-repo-${ENV}/node.tar.gz . &&
                      tar -xf node.tar.gz
                  volumeMounts:
                  - name: my-s3-node-repo
                    mountPath: /root/.npm
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: nodejs
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/nodejs:12.18.2
                  imagePullPolicy: Always
                  resources:
                    requests:
                      ephemeral-storage: "3Gi"
                  command:
                  - sleep
                  args:
                  - "6000"
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                  - name: my-s3-node-repo
                    mountPath: /root/.npm
                - name: docker
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/docker:19.03.14
                  imagePullPolicy: Always
                  resources:
                    requests:
                      ephemeral-storage: "3Gi"
                  command:
                  - sleep
                  args:
                  - "6000"
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                serviceAccountName: "jenkins"
                volumes:
                - name: dockersock
                  hostPath:
                    path: /var/run/docker.sock
                - name: my-s3-node-repo
                  emtpyDir: {}
"""){
		body.call()
	}
}

public void awscliTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: awscliTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "awscli: jenkins/awscli:latest"
              spec:
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: awscli
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/awscli:latest
                  command:
                  - sleep
                  args:
                  - "6000"
                serviceAccountName: "jenkins"
"""){
		body.call()
	}
}

public void golangTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: golangTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "golang: x/golangtemplate:latest,helm: tools/helm:latest"
              spec:
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: helm
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/tools/helm
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  - name: AWS
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                - name: golang
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/x/golangtemplate
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  - name: AWS
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                serviceAccountName: "jenkins"
                volumes:
                - name: dockersock
                  hostPath:
                    path: /var/run/docker.sock
"""){
		body.call()
	}
}

public void deployTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: deployTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "helm: tools/helm:latest,helmsman: tools/helmsman:latest"
              spec:
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: helm
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/tools/helm
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  - name: AWS
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                - name: helmsman
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/tools/helmsman
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  - name: AWS
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                serviceAccountName: "jenkins"
                volumes:
                - name: dockersock
                  hostPath:
                    path: /var/run/docker.sock
"""){
		body.call()
	}
}

public void deployCronTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: deployCronTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "helm: tools/helm:latest,helmsman: tools/helmsman:latest"
              spec:
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: helm
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/tools/helm
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  - name: AWS
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                - name: helmsman
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/tools/helmsman
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  - name: AWS
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                serviceAccountName: "jenkins"
                volumes:
                - name: dockersock
                  hostPath:
                    path: /var/run/docker.sock
"""){
		body.call()
	}
}

public void kickoffTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: kickoffTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "helm: tools/helm:latest"
              spec:
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: helm
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/tools/helm
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
"""){
		body.call()
	}
}

public void packerTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: packerTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "packer: tools/packer:latest"
              spec:
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: packer
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/tools/packer
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
"""){
		body.call()
	}
}

public void devopsTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: devopsTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "mysql: jenkins/mysql:latest,terragrunt: tools/terragrunt:1.0.3"
              spec:
                hostNetwork: true
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: mysql
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/mysql
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                - name: terragrunt
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/tools/terragrunt:1.0.3
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                serviceAccountName: "jenkins"
"""){
		body.call()
	}
}

public void dockerTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: dockerTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "docker: jenkins/docker:19.03.14"
              spec:
                hostNetwork: true
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: docker
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/docker:19.03.14
                  imagePullPolicy: Always
                  resources:
                    requests:
                      ephemeral-storage: "3Gi"
                  command:
                  - sleep
                  args:
                  - "6000"
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                serviceAccountName: "jenkins"
                volumes:
                - name: dockersock
                  hostPath:
                    path: /var/run/docker.sock
"""){
		body.call()
	}
}

public void helmChartBuilder(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "template_name: helmChartBuilder,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "helm-chart-builder: jenkins/helm-chart-builder:latest"
              spec:
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: helm-chart-builder
                  image: <ACCOUNT>.dkr.ecr.us-west-2.amazonaws.com/jenkins/helm-chart-builder:latest
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                serviceAccountName: "jenkins"
"""){
		body.call()
	}
}

public void terragruntTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "name: terragruntTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "terragrunt: tools/terragrunt:1.0.3"
              spec:
                hostNetwork: true
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: terragrunt
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/tools/terragrunt:1.0.3
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                serviceAccountName: "jenkins"
"""){
		body.call()
	}
}

public void qaTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "name: qaTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "qa-main: jenkins/qa-main:latest"
              spec:
                hostNetwork: true
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: qa-main
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/jenkins/qa-main
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                  - name: AWS_ACCESS_KEY_ID
                    valueFrom:
                      secretKeyRef:
                        key: aws-access-key-id
                        name: aws-account
                        optional: false
                  - name: AWS_SECRET_ACCESS_KEY
                    valueFrom:
                      secretKeyRef:
                        key: aws-secret-access-key
                        name: aws-account
                        optional: false
                  - name: AWS
                  volumeMounts:
                  - name: dockersock
                    mountPath: /var/run/docker.sock
                serviceAccountName: "jenkins"
                volumes:
                - name: dockersock
                  hostPath:
                    path: /var/run/docker.sock
"""){
		body.call()
	}
}

public void devopsapiTemplate(body) {
	podTemplate(cloud: "${env.CLUSTER_NAME}", yaml: """
              apiVersion: v1
              kind: Pod
              metadata:
                annotations:
                  podtemplate_info: "name: devopsapiTemplate,account: ${env.ACCOUNT},region: ${env.REGION}"
                  podtemplate_containers: "python: python:3.9.10,nginx: nginx:1.20.2"
              spec:
                hostNetwork: true
                containers:
                - name: info
                  image: busybox:latest
                  imagePullPolicy: IfNotPresent
                  command:
                  - sleep
                  args:
                  - "6000"
                  env:
                    - name: PODTEMPLATE_CONTAINERS
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_containers']
                    - name: PODTEMPLATE_INFO
                      valueFrom:
                        fieldRef:
                          fieldPath: metadata.annotations['podtemplate_info']
                    - name: NODENAME
                      valueFrom:
                        fieldRef:
                          fieldPath: spec.nodeName
                - name: python
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/python/python:3.9.10
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                - name: nginx
                  image: ${env.ACCOUNT}.dkr.ecr.${env.REGION}.amazonaws.com/python/nginx:1.20.2
                  imagePullPolicy: Always
                  command:
                  - sleep
                  args:
                  - "6000"
                serviceAccountName: "jenkins"
"""){
		body.call()
	}
}

return this
