{{- define "ui_service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "ui_service.fullname" -}}
{{- include "ui_service.name" . -}}
{{- end -}}
