// override the default code generator here
trait Codegen extends MyCodegen {
  // set the models requiring code generation here
  override def tableNames = List(
    "persistence_metadata",
    "persistence_journal",
    "persistence_snapshot")
}
