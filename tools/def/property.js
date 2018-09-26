obj = {
  name: 'property', // наименование актора
  package: 'annette.core.domain.property', // корневой пакет, опционально
  // modelPackage: 'annette.core.domain.property.model',  // пакет с моделью данных
  // actorPackage: 'annette.uic.staff',  // пакет с моделью данных
  actorName: 'PropertyActor',
  stateName: 'PropertyState',
  objectName: 'PropertyService',

  imports: ['annette.core.domain.application.Application',
    'annette.core.domain.language.model.Language',
    'annette.core.domain.tenancy.model.{Tenant, User}'],

  entities: [
    {
      name: 'property',
      pluralName: 'properties',
      commands: [
        {create: true, verb: "set", pstVerb: "set"},
        {
          del: true,
          verb: "remove",
          pstVerb: "removed",
        },
      ],
      queries: [
        {byId: true},
        {all: true}
      ]
    }
  ]
}