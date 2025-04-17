#!/bin/bash

# Initialize LocalStack SQS queue for local development

echo "Initializing LocalStack SQS queue..."

# Create the user registration queue
awslocal --endpoint-url=http://localhost:4566 sqs create-queue \
  --queue-name userman-local-register-event.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=true
echo "LocalStack SQS queue initialized successfully!"