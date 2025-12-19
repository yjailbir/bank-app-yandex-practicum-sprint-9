pipeline {
    agent any

    stages {
        stage('build jar') {
            steps {
                powershell '''
                Set-Location "F:\\Work\\Projects\\IdeaProjects\\bank-app-yandex-practicum-sprint-9"
                .\\mvnw.cmd clean package
                '''
            }
        }

        stage('switch context') {
            steps {
        powershell '''
        $dockerEnv = minikube -p minikube docker-env --shell powershell
        $dockerEnv | Where-Object { $_ -match '=' } | ForEach-Object { Invoke-Expression $_ }
        '''
            }
        }

        stage('build docker') {
            steps {
                powershell '''
                docker build -t accounts-service:1.0 .\\accounts-service
                docker build -t blocker-service:1.0 .\\blocker-service
                docker build -t cash-service:1.0 .\\cash-service
                docker build -t exchange-generator-service:1.0 .\\exchange-generator-service
                docker build -t exchange-service:1.0 .\\exchange-service
                docker build -t notification-service:1.0 .\\notification-service
                docker build -t transfer-service:1.0 .\\transfer-service
                docker build -t ui-service:1.0 .\\ui-service
                '''
            }
        }

        stage('deploy') {
            steps {
                powershell '''
                kubectl create namespace monitoring
                helm install prometheus-stack prometheus-community/kube-prometheus-stack -n monitoring
                helm install grafana grafana/grafana -n monitoring
                helm install logstash elastic/logstash -f logstash-values.yaml
                helm install elasticsearch elastic/elasticsearch
                helm install bank-app ./bank-app
                kubectl apply -f prometheusrule.yaml
                '''
            }
        }
    }
}
