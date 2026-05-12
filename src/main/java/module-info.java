module AISandbox.Server.main {
  requires ch.qos.logback.classic;
  requires ch.qos.logback.core;
  requires com.google.protobuf;
  requires javafx.controls;
  requires javafx.fxml;
  requires static lombok;
  requires org.apache.commons.cli;
  requires org.apache.commons.lang3;
  requires org.apache.commons.statistics.descriptive;
  requires org.slf4j;
  requires javafx.swing;
  requires io.opentelemetry.api;
  requires io.opentelemetry.sdk;
  requires io.opentelemetry.exporter.otlp;
  requires io.opentelemetry.sdk.logs;
  requires org.apache.commons.rng.api;
  requires org.apache.commons.rng.simple;
  requires org.apache.commons.rng.sampling;

  opens dev.aisandbox.launcher to javafx.graphics;
  opens dev.aisandbox.server.fx to javafx.fxml;
}