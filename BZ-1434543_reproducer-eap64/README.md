1. Start EAP with -Dresteasy.allowGzip=true
2. Run:
mvn clean test -Dresteasy.allowGzip=true -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog -Dorg.apache.commons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG

