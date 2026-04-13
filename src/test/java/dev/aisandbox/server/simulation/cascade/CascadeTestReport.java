/*
 * AI Sandbox - This program is free software: you can redistribute it and/or modify it under the
 * terms of version 3 of the GNU General Public License. See the README and LICENCE files for
 * more information.
 */

package dev.aisandbox.server.simulation.cascade;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import dev.aisandbox.server.simulation.cascade.model.CascadeBoard;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for generating HTML test reports for Cascade board tests.
 *
 * <p>Each test method calls {@link #record} to capture the input board, expected pattern,
 * actual board, and pass/fail status. At the end of the test class, {@link #writeReport}
 * renders all captured results to an HTML file with coloured board grids.
 */
public final class CascadeTestReport {

  private CascadeTestReport() {
  }

  /** A single test result record. */
  public static class TestRecord {
    public final String testName;
    public final boolean passed;
    public final String[] inputRows;
    public final String inputInfo;
    public final String[] expectedRows;
    public final String expectedInfo;
    public final String[] actualRows;
    public final String actualInfo;
    public final String errorMessage;

    public TestRecord(String testName, boolean passed, String[] inputRows, String inputInfo,
        String[] expectedRows, String expectedInfo, String[] actualRows, String actualInfo,
        String errorMessage) {
      this.testName = testName;
      this.passed = passed;
      this.inputRows = inputRows;
      this.inputInfo = inputInfo;
      this.expectedRows = expectedRows;
      this.expectedInfo = expectedInfo;
      this.actualRows = actualRows;
      this.actualInfo = actualInfo;
      this.errorMessage = errorMessage;
    }
  }

  private static final List<TestRecord> RESULTS = new ArrayList<>();

  /** Clears all recorded results. Call from {@code @BeforeAll}. */
  public static void reset() {
    RESULTS.clear();
  }

  /**
   * Records a test result for inclusion in the HTML report.
   *
   * @param testName     the display name of the test
   * @param passed       whether the test passed
   * @param inputBoard   the board state before the operation (may be null for exception tests)
   * @param expectedRows the expected pattern rows (may be null for exception tests)
   * @param actualBoard  the board state after the operation (may be null if exception thrown)
   * @param errorMessage error details if the test failed (may be null)
   */
  public static void record(String testName, boolean passed, CascadeBoard inputBoard,
      String[] expectedRows, CascadeBoard actualBoard, String errorMessage) {
    record(testName, passed, inputBoard, expectedRows, null, actualBoard, errorMessage);
  }

  /**
   * Records a test result with expected score/multiplier information.
   *
   * @param testName     the display name of the test
   * @param passed       whether the test passed
   * @param inputBoard   the board state before the operation (may be null for exception tests)
   * @param expectedRows the expected pattern rows (may be null for exception tests)
   * @param expectedInfo expected score/multiplier text (may be null if not checked)
   * @param actualBoard  the board state after the operation (may be null if exception thrown)
   * @param errorMessage error details if the test failed (may be null)
   */
  public static void record(String testName, boolean passed, CascadeBoard inputBoard,
      String[] expectedRows, String expectedInfo, CascadeBoard actualBoard, String errorMessage) {
    String[] inRows = inputBoard != null
        ? CascadeBoardUtils.serialiseBoard(inputBoard).toArray(new String[0]) : null;
    String inInfo = inputBoard != null
        ? String.format("Score: %d  Moves: %d  Multiplier: %d",
        inputBoard.getScore(), inputBoard.getMovesRemaining(), inputBoard.getMultiplier())
        : "";
    String[] actRows = actualBoard != null
        ? CascadeBoardUtils.serialiseBoard(actualBoard).toArray(new String[0]) : null;
    String actInfo = actualBoard != null
        ? String.format("Score: %d  Moves: %d  Multiplier: %d",
        actualBoard.getScore(), actualBoard.getMovesRemaining(), actualBoard.getMultiplier())
        : "";
    RESULTS.add(new TestRecord(testName, passed, inRows, inInfo,
        expectedRows, expectedInfo != null ? expectedInfo : "", actRows, actInfo, errorMessage));
  }

  /**
   * Records a test that expected an exception.
   *
   * @param testName      the display name of the test
   * @param passed        whether the expected exception was thrown
   * @param inputBoard    the board state before the operation
   * @param expectedError the expected exception description
   * @param errorMessage  actual error details if the test failed
   */
  public static void recordException(String testName, boolean passed, CascadeBoard inputBoard,
      String expectedError, String errorMessage) {
    String[] inRows = inputBoard != null
        ? CascadeBoardUtils.serialiseBoard(inputBoard).toArray(new String[0]) : null;
    String inInfo = inputBoard != null
        ? String.format("Score: %d  Moves: %d  Multiplier: %d",
        inputBoard.getScore(), inputBoard.getMovesRemaining(), inputBoard.getMultiplier())
        : "";
    RESULTS.add(new TestRecord(testName, passed, inRows, inInfo,
        new String[]{expectedError}, "", null, "", errorMessage));
  }

  /**
   * Writes the HTML report to the specified file path under {@code build/test/cascade/}.
   *
   * @param fileName the output file name (e.g. "makeMove.html")
   * @param title    the report title
   */
  public static void writeReport(String fileName, String title) throws IOException {
    File outputFile = new File("build/test/cascade/" + fileName);
    outputFile.getParentFile().mkdirs();

    Template tmpl = Mustache.compiler().compile(REPORT_TEMPLATE);

    List<Map<String, Object>> testData = new ArrayList<>();
    int passCount = 0;
    int failCount = 0;
    for (TestRecord r : RESULTS) {
      Map<String, Object> entry = new HashMap<>();
      entry.put("testName", r.testName);
      entry.put("passed", r.passed);
      entry.put("badge", r.passed ? "PASS" : "FAIL");
      entry.put("badgeClass", r.passed ? "pass" : "fail");
      if (r.passed) {
        passCount++;
      } else {
        failCount++;
      }

      if (r.inputRows != null) {
        entry.put("hasInput", true);
        entry.put("inputGrid", buildGrid(r.inputRows));
        entry.put("inputInfo", r.inputInfo);
      }

      if (r.expectedRows != null && r.actualRows == null) {
        // Exception test — show expected message only
        entry.put("isExceptionTest", true);
        entry.put("expectedMessage", String.join(", ", r.expectedRows));
      } else {
        entry.put("isExceptionTest", false);
        if (r.expectedRows != null) {
          entry.put("hasExpected", true);
          entry.put("expectedGrid", buildGrid(r.expectedRows));
          entry.put("hasExpectedInfo",
              r.expectedInfo != null && !r.expectedInfo.isEmpty());
          entry.put("expectedInfo", r.expectedInfo);
        }
        if (r.actualRows != null) {
          entry.put("hasActual", true);
          entry.put("actualGrid",
              buildGridWithMismatch(r.actualRows, r.expectedRows));
          entry.put("actualInfo", r.actualInfo);
        }
      }

      entry.put("hasError", r.errorMessage != null && !r.errorMessage.isEmpty());
      entry.put("errorMessage", r.errorMessage);
      testData.add(entry);
    }

    Map<String, Object> context = new HashMap<>();
    context.put("title", title);
    context.put("tests", testData);
    context.put("passCount", passCount);
    context.put("failCount", failCount);
    context.put("totalCount", passCount + failCount);

    try (PrintWriter out = new PrintWriter(new FileWriter(outputFile))) {
      out.print(tmpl.execute(context));
    }
  }

  // ── Grid building ───────────────────────────────────────────────────────

  private static List<List<Map<String, String>>> buildGrid(String[] rows) {
    List<List<Map<String, String>>> grid = new ArrayList<>();
    for (String row : rows) {
      List<Map<String, String>> gridRow = new ArrayList<>();
      String[] tokens = row.split(" ");
      for (String token : tokens) {
        Map<String, String> cell = new HashMap<>();
        cell.put("token", token);
        cell.put("css", cssClass(token));
        gridRow.add(cell);
      }
      grid.add(gridRow);
    }
    return grid;
  }

  private static List<List<Map<String, String>>> buildGridWithMismatch(
      String[] actualRows, String[] expectedRows) {
    List<List<Map<String, String>>> grid = new ArrayList<>();
    for (int y = 0; y < actualRows.length; y++) {
      List<Map<String, String>> gridRow = new ArrayList<>();
      String[] actTokens = actualRows[y].split(" ");
      String[] expTokens = expectedRows != null && y < expectedRows.length
          ? expectedRows[y].split(" ") : new String[0];
      for (int x = 0; x < actTokens.length; x++) {
        Map<String, String> cell = new HashMap<>();
        cell.put("token", actTokens[x]);
        String css = cssClass(actTokens[x]);
        if (x < expTokens.length && !tokensMatch(expTokens[x], actTokens[x])) {
          css += " mismatch";
        }
        cell.put("css", css);
        gridRow.add(cell);
      }
      grid.add(gridRow);
    }
    return grid;
  }

  private static boolean tokensMatch(String expected, String actual) {
    if (expected.equals(actual)) {
      return true;
    }
    // Wildcard: any occupied cell
    if (expected.equals("??")) {
      return !actual.equals("..");
    }
    // Wildcard: specific type, any colour
    if (expected.charAt(0) == '?') {
      return actual.length() == 2
          && Character.toLowerCase(actual.charAt(1)) == expected.charAt(1);
    }
    return false;
  }

  private static String cssClass(String token) {
    if (token.equals("..")) {
      return "empty";
    }
    if (token.equals("##")) {
      return "stone";
    }
    if (token.equalsIgnoreCase("xx")) {
      return "prism";
    }
    if (token.startsWith("?")) {
      return "wildcard";
    }
    char colourChar = Character.toLowerCase(token.charAt(0));
    String base = switch (colourChar) {
      case 'r' -> "red";
      case 'b' -> "blue";
      case 'g' -> "green";
      case 'y' -> "yellow";
      case 'p' -> "purple";
      default -> "unknown";
    };
    char typeChar = Character.toLowerCase(token.charAt(1));
    if (typeChar == 'i') {
      return base + " ice";
    }
    if (typeChar == 'b' || typeChar == 'h' || typeChar == 'v') {
      return base + " special";
    }
    if (Character.isUpperCase(token.charAt(0)) && Character.isUpperCase(token.charAt(1))) {
      return base + " activated";
    }
    return base;
  }

  // ── Mustache template ──────────────────────────────────────────────────

  private static final String REPORT_TEMPLATE = """
      <!DOCTYPE html>
      <html>
      <head>
      <meta charset="UTF-8">
      <title>{{title}}</title>
      <style>
        body { font-family: 'Segoe UI', Arial, sans-serif; background: #1a1a2e; color: #eee;
               margin: 20px; }
        h1 { color: #e0e0e0; border-bottom: 2px solid #444; padding-bottom: 10px; }
        .summary { font-size: 1.1em; margin-bottom: 20px; }
        .summary .pass-count { color: #4caf50; font-weight: bold; }
        .summary .fail-count { color: #f44336; font-weight: bold; }
        .test-card { background: #16213e; border-radius: 8px; margin: 16px 0; padding: 16px;
                     border-left: 4px solid #444; }
        .test-card.passed { border-left-color: #4caf50; }
        .test-card.failed { border-left-color: #f44336; }
        .test-header { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
        .test-name { font-size: 1.1em; font-weight: 600; }
        .badge { padding: 2px 10px; border-radius: 4px; font-size: 0.85em; font-weight: bold; }
        .badge.pass { background: #4caf50; color: #fff; }
        .badge.fail { background: #f44336; color: #fff; }
        .boards { display: flex; gap: 24px; flex-wrap: wrap; }
        .board-section { text-align: center; }
        .board-label { font-weight: 600; margin-bottom: 6px; color: #aaa; font-size: 0.9em; }
        table.board { border-collapse: collapse; }
        table.board td { width: 36px; height: 36px; text-align: center; font-size: 11px;
                         font-family: 'Consolas', 'Courier New', monospace; font-weight: bold;
                         border: 1px solid #333; }
        .empty { background: #2a2a3a; color: #555; }
        .stone { background: #6e6e6e; color: #fff; }
        .prism { background: #ffffff; color: #333; }
        .wildcard { background: #555; color: #ff0; }
        .red { background: #d23232; color: #fff; }
        .blue { background: #3264d2; color: #fff; }
        .green { background: #32af3c; color: #fff; }
        .yellow { background: #d2be1e; color: #333; }
        .purple { background: #9632c8; color: #fff; }
        .ice { opacity: 0.6; }
        .special { font-style: italic; }
        .activated { outline: 2px solid #ff0; outline-offset: -2px; }
        .mismatch { outline: 3px solid #f44336 !important; outline-offset: -3px; }
        .board-info { font-size: 0.8em; color: #888; margin-top: 4px; }
        .error-msg { background: #3a1a1a; color: #f88; padding: 8px 12px; border-radius: 4px;
                     margin-top: 10px; font-family: monospace; font-size: 0.85em;
                     white-space: pre-wrap; max-height: 200px; overflow-y: auto; }
        .expected-msg { background: #1a2a3a; color: #8cf; padding: 8px 12px; border-radius: 4px;
                        margin-top: 8px; font-family: monospace; }
      </style>
      </head>
      <body>
      <h1>{{title}}</h1>
      <div class="summary">
        Tests: {{totalCount}} |
        <span class="pass-count">{{passCount}} passed</span> |
        <span class="fail-count">{{failCount}} failed</span>
      </div>

      {{#tests}}
      <div class="test-card {{#passed}}passed{{/passed}}{{^passed}}failed{{/passed}}">
        <div class="test-header">
          <span class="badge {{badgeClass}}">{{badge}}</span>
          <span class="test-name">{{testName}}</span>
        </div>

        <div class="boards">
          {{#hasInput}}
          <div class="board-section">
            <div class="board-label">Input</div>
            <table class="board">
              {{#inputGrid}}
              <tr>
                {{#.}}<td class="{{css}}">{{token}}</td>{{/.}}
              </tr>
              {{/inputGrid}}
            </table>
            <div class="board-info">{{inputInfo}}</div>
          </div>
          {{/hasInput}}

          {{#isExceptionTest}}
          <div class="board-section">
            <div class="board-label">Expected</div>
            <div class="expected-msg">{{expectedMessage}}</div>
          </div>
          {{/isExceptionTest}}

          {{^isExceptionTest}}
          {{#hasExpected}}
          <div class="board-section">
            <div class="board-label">Expected</div>
            <table class="board">
              {{#expectedGrid}}
              <tr>
                {{#.}}<td class="{{css}}">{{token}}</td>{{/.}}
              </tr>
              {{/expectedGrid}}
            </table>
            {{#hasExpectedInfo}}<div class="board-info">{{expectedInfo}}</div>{{/hasExpectedInfo}}
          </div>
          {{/hasExpected}}

          {{#hasActual}}
          <div class="board-section">
            <div class="board-label">Actual</div>
            <table class="board">
              {{#actualGrid}}
              <tr>
                {{#.}}<td class="{{css}}">{{token}}</td>{{/.}}
              </tr>
              {{/actualGrid}}
            </table>
            <div class="board-info">{{actualInfo}}</div>
          </div>
          {{/hasActual}}
          {{/isExceptionTest}}
        </div>

        {{#hasError}}
        <div class="error-msg">{{errorMessage}}</div>
        {{/hasError}}
      </div>
      {{/tests}}

      </body>
      </html>
      """;
}
