{{- define "exchange_generator_service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "exchange_generator_service.fullname" -}}
{{- include "exchange_generator_service.name" . -}}
{{- end -}}
