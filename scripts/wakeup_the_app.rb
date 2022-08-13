require 'net/http'

host = ENV["APP_HOSTNAME"]
path = "/api/products"

instance = Net::HTTP::new(host, 443)
instance.use_ssl = true
res = instance.head(path)
puts res.header['Content-Type']