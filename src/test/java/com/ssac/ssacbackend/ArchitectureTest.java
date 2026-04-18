package com.ssac.ssacbackend;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * 레이어 의존성 규칙을 기계적으로 강제하는 ArchUnit 테스트.
 *
 * 규칙 변경 시:
 *   1. docs/architecture.md 를 먼저 수정
 *   2. 이 파일의 규칙을 동기화
 *   3. docs/decisions/ 에 ADR 추가
 *
 * 빌드 실패 시 remediation:
 *   - 금지된 import를 제거하고 올바른 레이어를 통해 접근하라.
 *   - 예) Controller -> Repository 직접 호출은 Service를 통해야 한다.
 *   - 자세한 규칙: docs/architecture.md
 */
class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.ssac.ssacbackend";
    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter().importPackages(BASE_PACKAGE);
    }

    @Test
    @DisplayName("레이어 의존성 방향 규칙")
    void layerDependencyRule() {
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy(BASE_PACKAGE + ".controller..")
            .layer("Service").definedBy(BASE_PACKAGE + ".service..")
            .layer("Repository").definedBy(BASE_PACKAGE + ".repository..")
            .layer("Domain").definedBy(BASE_PACKAGE + ".domain..")
            .layer("Dto").definedBy(BASE_PACKAGE + ".dto..")
            .layer("Config").definedBy(BASE_PACKAGE + ".config..")
            .layer("Common").definedBy(BASE_PACKAGE + ".common..")
            .whereLayer("Controller").mayOnlyAccessLayers("Service", "Dto", "Common", "Config")
            .whereLayer("Service").mayOnlyAccessLayers("Repository", "Domain", "Dto", "Common", "Config")
            .whereLayer("Repository").mayOnlyAccessLayers("Domain", "Common")
            .whereLayer("Domain").mayNotAccessAnyLayer()
            .check(classes);
    }

    @Test
    @DisplayName("[LINT-001] Controller에서 Repository 직접 호출 금지")
    void controllerMustNotDependOnRepository() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..repository..")
            .because(
                "[LINT-001] Controller는 Repository를 직접 import할 수 없습니다. " +
                "remediation: Service 레이어를 통해 데이터에 접근하세요. " +
                "참고: docs/architecture.md"
            );
        rule.check(classes);
    }

    @Test
    @DisplayName("[LINT-002] Domain에서 상위 레이어 import 금지")
    void domainMustNotDependOnUpperLayers() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..service..", "..controller..", "..repository..")
            .because(
                "[LINT-002] Domain 클래스는 순수해야 합니다. " +
                "remediation: 비즈니스 로직은 Service로, 데이터 접근은 Repository로 이동하세요. " +
                "참고: docs/architecture.md"
            );
        rule.check(classes);
    }

    @Test
    @DisplayName("[LINT-003] @Transactional은 Service 레이어에만 허용")
    void transactionalOnlyInService() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..controller..")
            .should().beAnnotatedWith(org.springframework.transaction.annotation.Transactional.class)
            .because(
                "[LINT-003] @Transactional은 Controller에 붙일 수 없습니다. " +
                "remediation: 트랜잭션 경계는 Service 메서드에 선언하세요. " +
                "참고: docs/conventions.md#트랜잭션-규칙"
            );
        rule.check(classes);
    }
}
