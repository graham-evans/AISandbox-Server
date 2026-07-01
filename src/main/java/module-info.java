module AISandbox.Server.main {
  requires org.apache.logging.log4j;
  requires org.apache.logging.log4j.core;
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
  requires com.fasterxml.jackson.databind;
  requires io.opentelemetry.sdk.common;

  opens dev.aisandbox.launcher to javafx.graphics;
  opens dev.aisandbox.server.fx to javafx.fxml;

  // Protobuf-generated classes require reflective access from com.google.protobuf.
  // Unqualified opens are used because jlink merges com.google.protobuf into dev.aisandbox.merged.module.
  opens dev.aisandbox.server.simulation.bandit.proto;
  opens dev.aisandbox.server.simulation.cascade.proto;
  opens dev.aisandbox.server.simulation.coingame.proto;
  opens dev.aisandbox.server.simulation.highlowcards.proto;
  opens dev.aisandbox.server.simulation.mancala.proto;
  opens dev.aisandbox.server.simulation.maze.proto;
  opens dev.aisandbox.server.simulation.mine.proto;
  opens dev.aisandbox.server.simulation.twisty.proto;
}