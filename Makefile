BRANCH=master
LEIN = ./utils/lein.sh
TEST_ARGS = midje
TMP_DIR := $(shell gmktemp -u 2>/dev/null || mktemp -u)
CLJ_FILES := $(shell find . -name "*.clj")
export LEIN_SNAPSHOTS_IN_RELEASE=1


all: uberjar

bump-major:
	${LEIN} ver bump :major

bump-minor:
	${LEIN} ver bump :minor

bump-patch:
	${LEIN} ver bump :patch

deps:
	@${LEIN} deps

minor-release: bump-minor tag-release

major-release: bump-major tag-release

patch-release: bump-patch tag-release

package:
	@-rm *.tar.gz
	@mkdir -p ${TMP_DIR}/fester && \
		cp -r resources $(shell find target -name "fester.jar") \
			${TMP_DIR}/fester && \
	cd ${TMP_DIR}/fester/.. && \
	tar cvzf fester.tar.gz fester
	@cp ${TMP_DIR}/fester.tar.gz .
	@rm -r ${TMP_DIR}

tag-release:
	git tag -a $(shell ${LEIN} ver) -m "New Release" && \
	git add project.clj resources/VERSION && \
	git commit -m ":sparkles: Bump to $(shell ${LEIN} ver) :sparkles:" && \
	git push origin $(BRANCH) && \
	git push --tags || \
	(git checkout project.clj resources/VERSION && \
	exit 1)

target/uberjar/fester.jar: ${CLJ_FILES}
	@${LEIN} uberjar

test: deps
	@${LEIN} ${TEST_ARGS}

uberjar: target/uberjar/fester.jar

.PHONY: \
	all \
	bump-major \
	bump-minor \
	bump-patch \
	bump \
	deps \
	major-release \
	minor-release \
	package \
	patch-release \
	tag-release \
	test \
	uberjar \
	ver
