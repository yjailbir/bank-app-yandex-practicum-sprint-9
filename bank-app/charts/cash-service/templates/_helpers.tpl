{{- define "cash_service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "cash_service.fullname" -}}
{{- include "cash_service.name" . -}}
{{- end -}}
