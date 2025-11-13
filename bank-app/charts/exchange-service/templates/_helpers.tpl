{{- define "exchange_service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "exchange_service.fullname" -}}
{{- include "exchange_service.name" . -}}
{{- end -}}
