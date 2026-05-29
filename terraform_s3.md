# Configuración de AWS S3 con Terraform

Este documento contiene la definición completa en **HCL (HashiCorp Configuration Language)** para aprovisionar el bucket de Amazon S3 necesario para almacenar las justificaciones y las imágenes de perfil de los empleados en **CloudTime v1**.

---

## Código de Terraform (`s3.tf`)

Crea un archivo llamado `s3.tf` con el siguiente contenido:

```hcl
# 1. Definición del Bucket de S3
resource "aws_s3_bucket" "cloudtime_storage" {
  bucket = var.s3_bucket_name

  tags = {
    Name        = "CloudTime Storage"
    Environment = var.environment
  }
}

# 2. Configuración de Control de Propiedad de Objetos (Object Ownership)
resource "aws_s3_bucket_ownership_controls" "cloudtime_ownership" {
  bucket = aws_s3_bucket.cloudtime_storage.id

  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}

# 3. Configuración de Bloqueo de Acceso Público
# Desactivamos el bloqueo estricto para permitir que la política de lectura pública funcione
resource "aws_s3_bucket_public_access_block" "cloudtime_public_access" {
  bucket = aws_s3_bucket.cloudtime_storage.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

# 4. Política del Bucket para permitir lectura pública anónima en carpetas específicas
# Permite mostrar las imágenes directamente en la aplicación web
resource "aws_s3_bucket_policy" "cloudtime_public_policy" {
  depends_on = [aws_s3_bucket_public_access_block.cloudtime_public_access]
  bucket     = aws_s3_bucket.cloudtime_storage.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid       = "PublicReadGetObjectInFolders"
        Effect    = "Allow"
        Principal = "*"
        Action    = "s3:GetObject"
        Resource = [
          "${aws_s3_bucket.cloudtime_storage.arn}/justificaciones/*",
          "${aws_s3_bucket.cloudtime_storage.arn}/empleados/*"
        ]
      }
    ]
  })
}

# 5. Configuración de CORS (Cross-Origin Resource Sharing)
# Permite que la aplicación frontend (React) consulte y cargue archivos de forma segura
resource "aws_s3_bucket_cors_configuration" "cloudtime_cors" {
  bucket = aws_s3_bucket.cloudtime_storage.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "HEAD", "PUT", "POST"]
    allowed_origins = ["*"] # Cambiar por tu dominio frontend en producción (ej. http://localhost:5173)
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

# 6. Creación de Carpetas Virtuales (Prefijos)
resource "aws_s3_object" "folder_justificaciones" {
  bucket       = aws_s3_bucket.cloudtime_storage.id
  key          = "justificaciones/"
  content_type = "application/x-directory"
}

resource "aws_s3_object" "folder_empleados" {
  bucket       = aws_s3_bucket.cloudtime_storage.id
  key          = "empleados/"
  content_type = "application/x-directory"
}

# --- Variables ---

variable "s3_bucket_name" {
  description = "Nombre único global para el bucket de S3"
  type        = string
  default     = "cloudtime-storage-bucket-dev"
}

variable "environment" {
  description = "Ambiente de despliegue"
  type        = string
  default     = "development"
}
```

---

## Instrucciones de Despliegue

1. Inicializa Terraform si no lo has hecho:
   ```bash
   terraform init
   ```
2. Revisa el plan de ejecución para confirmar la creación de los recursos:
   ```bash
   terraform plan -var="s3_bucket_name=mi-nombre-de-bucket-unico"
   ```
3. Aplica los cambios para crear la infraestructura en AWS:
   ```bash
   terraform apply -var="s3_bucket_name=mi-nombre-de-bucket-unico" -auto-approve
   ```
