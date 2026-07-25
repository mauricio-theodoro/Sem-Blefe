package br.com.semblefe.arquitetura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "br.com.semblefe",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaLimpaTest {

    @ArchTest
    static final ArchRule aplicacao_nao_depende_de_api_ou_infraestrutura =
            noClasses()
                    .that().resideInAPackage("..aplicacao..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..api..", "..infraestrutura..");

    @ArchTest
    static final ArchRule api_nao_acessa_infraestrutura =
            noClasses()
                    .that().resideInAPackage("..api..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..infraestrutura..");

    @ArchTest
    static final ArchRule controllers_ficam_na_camada_api =
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..api..");

    @ArchTest
    static final ArchRule modulos_nao_possuem_ciclos =
            slices()
                    .matching("br.com.semblefe.(*)..")
                    .should().beFreeOfCycles();
}
