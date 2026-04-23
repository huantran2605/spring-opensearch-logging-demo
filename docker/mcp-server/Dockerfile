FROM python:3.11-slim
RUN pip install opensearch-mcp-server-py
ENV OPENSEARCH_URL=http://opensearch:9200
ENV OPENSEARCH_USERNAME=admin
ENV OPENSEARCH_PASSWORD=admin
EXPOSE 9900
CMD ["python", "-m", "mcp_server_opensearch", "--transport", "stream", "--host", "0.0.0.0", "--port", "9900"]
