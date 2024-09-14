package com.dulno.server.cli;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import com.dulno.server.cli.command.CommandInjectionModule;

@RequiredArgsConstructor(staticName = "create")
public final class ServerCLIInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(CommandInjectionModule.create());
  }
}
