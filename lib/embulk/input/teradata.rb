Embulk::JavaPlugin.register_input(
  "teradata", "org.embulk.input.teradata.TeradataInputPlugin",
  File.expand_path('../../../../classpath', __FILE__))
