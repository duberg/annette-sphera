obj = {
  name: 'user', // наименование актора
  package: 'annette.core.services', // корневой пакет, опционально
  modelPackage: 'annette.core.domain.tenancy.model',  // пакет с моделью данных
  //actorPackage: 'annette.uic.staff',  // пакет с моделью данных
  actorName: 'UserActor',
  stateName: 'UserState',
  objectName: 'UserService',

  // imports: [ 'java.util.UUID' ],

  entities: [
    {
      name: 'user',
      pluralName: 'users',
      commands: [
        {create: true, params: 'entry: User, password: String'},
        {update: true},
        {del: true}
      ],
      queries: [
        {byId: true},
        {all: true}
      ]
    },
    {
      name: 'password',
      commands: [
        { update: true, params: 'userId: User.Id, password: String' }
      ],
      queries: [
        { suffix: 'ByLoginAndPassword', params: 'login: String, password: String'},
      ]
    },
    {
      name: 'userProperty',
      pluralName: 'userProperties',
      commands: [
        {
          update: true,
          verb: 'set',
          pstVerb: 'set',
          params: 'userId: User.Id, tenantId: Option[Tenant.Id], applicationId: Option[Application.Id], key: String, value: String'
        },
        {
          del: true,
          verb: 'remove',
          pstVerb: 'removed',
          params: 'userId: User.Id, tenantId: Option[Tenant.Id], applicationId: Option[Application.Id], key: String'
        },
      ],
      queries: [
        {byId: true, params: 'userId: User.Id, tenantId: Option[Tenant.Id], applicationId: Option[Application.Id], key: String'},
        {all: true}
      ]
    },
  ]
}