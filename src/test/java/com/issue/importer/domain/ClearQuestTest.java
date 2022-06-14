package com.issue.importer.domain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClearQuestTest extends AbstractCsvItem {

    public static final String TITLE = "Erweiterung der TESYS_ESF.xml bezüglich Eulynx-IDs (Origin: CFX00613569)";
    public static final String DESCRIPTION = """
            ## Description
            Es fehlen teilweise die IDs für Eulynx-Objekte in der TESYS_ESF.xml. Für jene Objekte die in Eulynx-SCI-CC definiert sind existieren solche bereits. \s
            Für jene, welche nur in Eulynx-SCI-RBC existieren (z.b. TVD section ID, IO Element ID, ESA ID, LSA ID, WA ID) sollen diese IDs ergänzt werden. \s
            Anmerkung: SCI-RBC und SCI-CC Schnittstellenspezifikationen sind angehängt. \s
            Stage2: \s
            ESA: Emergeny Stop Area (ESA) \s
            LSA: Local Shunting Area (LCPE / local control element ???) \s
            WA: Working Area Command Element (WACE) \s
            IO:  Ein-/ausgabe (zb. AVAL) \s
            TVD: Track Vacany Detection (EMEL)
                            
            ## System structure
            I005_Tools/Simulation VARUS-ESF_Basis VARUS-ESF R5.0 (TG 200 RBC ESF R5.0 - Z2)
                            
            ## CCB Notes
            State: Submitted by: John Doe (abcd1234) on 18 March 2022 11:21:59 \s
            Eine Umsetzung ist in erster Linie für das Projekt NO relevant (J. Kuester, W. Raffl). Hierfür muss die passende Pflegebereitstellung von VARUS-ESF bedacht werden. \s
            ==================================================================================
                            
            ## Notes
                            
                            
            ## Attachments
            52443616	EuLynx_SCI-CC_Interface_Specification_vD.pdf	2716103	SCI-CC Schnittstellenspezifikation
            }""";

    @Test
    void testFileReading() throws IOException {
        ApplicationSettings settings = new ApplicationSettings("CLEAR_QUEST", ";");
        List<IssueData> items = readIssueData(settings, "/QueryResult.csv");

        assertThat(items).hasSize(4);

        IssueData item = items.get(0);

        assertThat(item.title()).isEqualTo(TITLE);
        assertThat(item.description()).isEqualTo(DESCRIPTION);
    }
}