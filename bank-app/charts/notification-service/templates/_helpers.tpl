{{- define "notification_service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "notification_service.fullname" -}}
{{- include "notification_service.name" . -}}
{{- end -}}
