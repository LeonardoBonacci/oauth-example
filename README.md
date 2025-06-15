# oauth-example


```
curl -u my-client:my-secret \
  -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials"

curl -H "Authorization: Bearer PASTE_YOUR_ACCESS_TOKEN_HERE" \
    http://localhost:8080/hello

```
