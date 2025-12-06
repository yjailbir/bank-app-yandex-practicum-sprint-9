./mvnw clean package -DskipTests
minikube -p minikube docker-env --shell powershell | Invoke-Expression
docker build -t accounts-service:1.0 .\accounts-service
docker build -t blocker-service:1.0 .\blocker-service
docker build -t cash-service:1.0 .\cash-service
docker build -t exchange-generator-service:1.0 .\exchange-generator-service
docker build -t exchange-service:1.0 .\exchange-service
docker build -t notification-service:1.0 .\notification-service
docker build -t transfer-service:1.0 .\transfer-service
docker build -t ui-service:1.0 .\ui-service
kubectl create namespace monitoring
helm install prometheus-stack prometheus-community/kube-prometheus-stack -n monitoring
helm install grafana grafana/grafana -n monitoring
helm install logstash elastic/logstash -f logstash-values.yaml
helm install elasticsearch elastic/elasticsearch
helm install kibana elastic/kibana -f kibana-values.yaml
helm install bank-app ./bank-app
kubectl apply -f prometheusrule.yaml
<#kubectl port-forward svc/ui-service 8080:8080#>
<#kubectl port-forward service/zipkin 9411:9411#>
<#kubectl port-forward -n monitoring svc/prometheus-stack-grafana 3000:80#>
