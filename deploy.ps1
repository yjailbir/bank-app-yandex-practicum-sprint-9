./mvnw clean package
minikube -p minikube docker-env --shell powershell | Invoke-Expression
docker build -t accounts-service:1.0 .\accounts-service
docker build -t blocker-service:1.0 .\blocker-service
docker build -t cash-service:1.0 .\cash-service
docker build -t exchange-generator-service:1.0 .\exchange-generator-service
docker build -t exchange-service:1.0 .\exchange-service
docker build -t notification-service:1.0 .\notification-service
docker build -t transfer-service:1.0 .\transfer-service
docker build -t ui-service:1.0 .\ui-service
Set-Location .\k8s\
kubectl apply -f accounts-service.yml
kubectl apply -f blocker-service.yml
kubectl apply -f cash-service.yml
kubectl apply -f exchange-generator-service.yml
kubectl apply -f exchange-service.yml
kubectl apply -f notification-service.yml
kubectl apply -f transfer-service.yml
kubectl apply -f ui-service.yml
Start-Sleep -Seconds 60
kubectl port-forward svc/ui-service 8080:8080
