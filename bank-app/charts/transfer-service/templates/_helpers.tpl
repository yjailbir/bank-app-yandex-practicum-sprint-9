{{- define "transfer_service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "transfer_service.fullname" -}}
{{- include "transfer_service.name" . -}}
{{- end -}}
