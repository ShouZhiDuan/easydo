#!/bin/bash

# ==============================================================================
# Script to generate a self-signed certificate for local HTTPS development.
# ==============================================================================

set -e # Exit immediately if a command exits with a non-zero status.

# --- Configuration ---
# You can modify these variables as needed.
ALIAS="https"
KEYSTORE_PASS="password"
RESOURCES_DIR="src/main/resources"
KEYSTORE_NAME="keystore.p12"
CERT_NAME="certificate.pem"
VALIDITY_DAYS=365
KEY_ALG="RSA"
KEY_SIZE=2048
STORE_TYPE="PKCS12"

# Distinguished Name (DN) for the certificate
# CN: Common Name (e.g., your domain name)
# OU: Organizational Unit (e.g., your department)
# O: Organization (e.g., your company)
# L: Locality (e.g., your city)
# ST: State or Province
# C: Country
DNAME="CN=localhost, OU=Development, O=ExampleCorp, L=Anytown, ST=Anystate, C=US"

# --- Derived Variables ---
KEYSTORE_PATH="${RESOURCES_DIR}/${KEYSTORE_NAME}"
CERT_PATH="${RESOURCES_DIR}/${CERT_NAME}"

# --- Main Logic ---
echo "Starting certificate generation process..."

# Ensure the target directory exists
mkdir -p "${RESOURCES_DIR}"
echo "Resource directory ensured at '${RESOURCES_DIR}'."

# Check if keystore already exists
if [ -f "${KEYSTORE_PATH}" ]; then
    echo "Keystore already exists at '${KEYSTORE_PATH}'. Skipping generation."
else
    echo "Generating keystore and private key..."
    keytool -genkeypair \
        -alias "${ALIAS}" \
        -keyalg "${KEY_ALG}" \
        -keysize "${KEY_SIZE}" \
        -storetype "${STORE_TYPE}" \
        -keystore "${KEYSTORE_PATH}" \
        -validity "${VALIDITY_DAYS}" \
        -storepass "${KEYSTORE_PASS}" \
        -dname "${DNAME}"
    echo "Keystore generated successfully at '${KEYSTORE_PATH}'."
fi

# Check if certificate already exists
if [ -f "${CERT_PATH}" ]; then
    echo "Certificate already exists at '${CERT_PATH}'. Skipping export."
else
    echo "Exporting public certificate from keystore..."
    keytool -exportcert \
        -alias "${ALIAS}" \
        -keystore "${KEYSTORE_PATH}" \
        -storetype "${STORE_TYPE}" \
        -storepass "${KEYSTORE_PASS}" \
        -rfc \
        -file "${CERT_PATH}"
    echo "Certificate exported successfully to '${CERT_PATH}'."
fi

echo "--------------------------------------------------"
echo "Script finished successfully!"
echo "Keystore location: ${KEYSTORE_PATH}"
echo "Public certificate location: ${CERT_PATH}"
echo "You can now configure your Spring Boot application to use the keystore"
echo "and import the public certificate into your browser or system trust store."
echo "--------------------------------------------------" 