minikube -p minikube docker-env --shell powershell | Invoke-Expression
$serviceName = "ui-service"

$portForwardProcesses = Get-CimInstance Win32_Process | Where-Object {
    $_.CommandLine -like "*kubectl*port-forward*svc/$serviceName*"
}

foreach ($proc in $portForwardProcesses) {
    Stop-Process -Id $proc.ProcessId -Force
}

kubectl delete all --all
Start-Sleep -Seconds 5

docker ps -q | ForEach-Object { docker stop $_ }
docker ps -aq | ForEach-Object { docker rm $_ }
docker images -q | ForEach-Object { docker rmi -f $_ }
docker volume prune -f
docker system prune -f