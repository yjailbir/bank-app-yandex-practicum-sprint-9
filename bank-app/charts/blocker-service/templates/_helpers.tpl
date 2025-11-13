{{- define "blocker_service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "blocker_service.fullname" -}}
{{- include "blocker_service.name" . -}}
{{- end -}}
