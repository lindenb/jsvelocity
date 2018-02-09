SHELL=/bin/bash
this.makefile=$(lastword $(MAKEFILE_LIST))
this.dir=$(dir $(realpath ${this.makefile}))

#need local settings ? create a file 'local.mk' in this directory
ifneq ($(realpath local.mk),)
include $(realpath local.mk)
endif


src.dir=${this.dir}src/main/java
src.test.dir=${this.dir}src/test/java
generated.dir=${this.dir}src/main/generated-sources
tmp.dir=${this.dir}_tmp
tmp.mft=${tmp.dir}/META-INF/MANIFEST.MF
export dist.dir?=${this.dir}dist
ANT?=ant
JAVAC?=javac
JAVA?=java
JAR?=jar


lib.dir?=lib

EMPTY :=
SPACE := $(EMPTY) $(EMPTY)

commons.codec.jars = \
	$(lib.dir)/commons-codec/commons-codec/1.10/commons-codec-1.10.jar



apache.velocity.jars  =  \
	$(lib.dir)/org/apache/velocity/velocity-tools/2.0/velocity-tools-2.0.jar \
        $(lib.dir)/org/apache/velocity/velocity-engine-core/2.0/velocity-engine-core-2.0.jar \
        $(lib.dir)/org/apache/commons/commons-lang3/3.5/commons-lang3-3.5.jar \
        $(lib.dir)/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar \
        $(lib.dir)/org/slf4j/slf4j-simple/1.7.25/slf4j-simple-1.7.25.jar \
        $(lib.dir)/org/yaml/snakeyaml/1.19/snakeyaml-1.19.jar \
        $(lib.dir)/commons-digester/commons-digester/1.8/commons-digester-1.8.jar \
        $(lib.dir)/commons-collections/commons-collections/3.2.1/commons-collections-3.2.1.jar \
        $(lib.dir)/commons-logging/commons-logging/1.1/commons-logging-1.1.jar \
        $(lib.dir)/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar 

jcommander.jar= \
	$(lib.dir)/com/beust/jcommander/1.64/jcommander-1.64.jar


testng.jars = \
	$(lib.dir)/org/testng/testng/6.11/testng-6.11.jar \
	${jcommander.jar}

gson.jars  =  \
	$(lib.dir)/com/google/code/gson/gson/2.3.1/gson-2.3.1.jar


all_maven_jars = $(sort ${commons.codec.jars} ${apache.velocity.jars}  ${gson.jars} ${jcommander.jar} )

.PHONY: all jsvelocity test

all: test jsvelocity

test: jsvelocity  ${all_maven_jars} ${testng.jars} testng.xml
	rm -rf ${tmp.dir}
	mkdir -p ${tmp.dir}/META-INF ${dist.dir}
	${JAVAC} -d ${tmp.dir} -g -classpath "$(subst $(SPACE),:,$(filter %.jar,$^))" -sourcepath ${src.dir}  `find ${src.dir} ${src.test.dir} -type f -name "*.java"`
	java -cp  "$(subst $(SPACE),:,$(filter %.jar,$^)):${tmp.dir}" org.testng.TestNG testng.xml
	rm -rf ${tmp.dir}
	
${dist.dir}/jsvelocity.jar : jsvelocity ${testng.jars}
	


jsvelocity: ${all_maven_jars}
	rm -rf ${tmp.dir}
	mkdir -p ${tmp.dir}/META-INF ${dist.dir}
	#expand
	$(foreach J,$(filter %.jar,$^),unzip -o ${J} -d ${tmp.dir};)
	#compile
	${JAVAC} -d ${tmp.dir} -g -classpath "$(subst $(SPACE),:,$(filter %.jar,$^))" -sourcepath ${src.dir}  `find ${src.dir} -type f -name "*.java"`
	#create META-INF/MANIFEST.MF
	echo "Manifest-Version: 1.0" > ${tmp.mft}
	echo "Main-Class: com.github.lindenb.jsvelocity.JSVelocity" >> ${tmp.mft}
	echo -n "Git-Hash: " >> ${tmp.mft}
	$(if $(realpath .git/refs/heads/master),cat $(realpath .git/refs/heads/master), echo "undefined")  >> ${tmp.mft}
	echo -n "Compile-Date: " >> ${tmp.mft}
	date +%Y-%m-%d:%H-%m-%S >> ${tmp.mft}
	#create jar
	${JAR} cfm ${dist.dir}/jsvelocity.jar ${tmp.mft}  -C ${tmp.dir} .
	#create bash executable
	echo '#!/bin/bash' > ${dist.dir}/jsvelocity
	echo '${JAVA} -Dfile.encoding=UTF8 -Xmx500m $(if ${http.proxy.host},-Dhttp.proxyHost=${http.proxy.host})  $(if ${http.proxy.port},-Dhttp.proxyPort=${http.proxy.port}) -cp "${dist.dir}/jsvelocity.jar" com.github.lindenb.jsvelocity.JSVelocity  $$*' >> ${dist.dir}/jsvelocity
	chmod  ugo+rx ${dist.dir}/jsvelocity
	#cleanup
	rm -rf ${tmp.dir}


${all_maven_jars} ${testng.jars}  : 
	mkdir -p $(dir $@) && wget -O "$@" "http://central.maven.org/maven2/$(patsubst ${lib.dir}/%,%,$@)"

