{{- define "accounts_service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "accounts_service.fullname" -}}
{{- include "accounts_service.name" . -}}
{{- end -}}
