minikube -p minikube docker-env --shell powershell | Invoke-Expression
$serviceName = "ui-service"

$portForwardProcesses = Get-CimInstance Win32_Process | Where-Object {
    $_.CommandLine -like "*kubectl*port-forward*svc/$serviceName*"
}

foreach ($proc in $portForwardProcesses) {
    Stop-Process -Id $proc.ProcessId -Force
}

helm uninstall bank-app
helm uninstall prometheus-stack
helm uninstall grafana
kubectl delete pvc -l app=kafka
Start-Sleep -Seconds 5

$myImages = @(
    "accounts-service:1.0",
    "blocker-service:1.0",
    "cash-service:1.0",
    "exchange-generator-service:1.0",
    "exchange-service:1.0",
    "notification-service:1.0",
    "transfer-service:1.0",
    "ui-service:1.0"
)

foreach ($img in $myImages) {
    $containers = docker ps -aq --filter "ancestor=$img"
    if ($containers) {
        docker stop $containers
        docker rm $containers
    }
}

foreach ($img in $myImages) {
    docker rmi -f $img
}

docker volume prune -f
docker system prune -f --filter "label!=io.kubernetes.container.name"
