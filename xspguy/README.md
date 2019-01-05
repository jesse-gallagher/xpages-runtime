# Build
mvn clean package && docker build -t org.darwino/xspguy .

# RUN

docker rm -f xspguy || true && docker run -d -p 8080:8080 -p 4848:4848 --name xspguy org.darwino/xspguy 