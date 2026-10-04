package net.taskwolf.server.cli;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import net.taskwolf.server.cli.command.CommandInjectionModule;

@RequiredArgsConstructor(staticName = "create")
public final class ServerCLIInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(CommandInjectionModule.create());
  }
}
