% ---
% --- HECHOS (FACTS)
% ---

% --- Artistas Base y sus roles ---
% (Agrupamos todos los 'base_member' primero)
base_member(brian_may).
base_member(roger_taylor).
base_member(john_deacon).

% (Agrupamos todos los 'artista' después)
artista(brian_may, [guitarra_electrica, voz_secundaria]).
artista(roger_taylor, [bateria, voz_secundaria]).
artista(john_deacon, [bajo]).

% --- Roles requeridos por el recital (de recital.json)
% (Se definen como hechos individuales para que Prolog pueda contarlos)

% Somebody to Love
rol_requerido(voz_principal).
rol_requerido(guitarra_electrica).
rol_requerido(bajo).
rol_requerido(bateria).
rol_requerido(piano).

% We Will Rock You
rol_requerido(voz_principal).
rol_requerido(guitarra_electrica).
rol_requerido(bajo).
rol_requerido(bateria).

% These Are the Days of Our Lives
rol_requerido(voz_principal).
rol_requerido(guitarra_electrica).
rol_requerido(bajo).
rol_requerido(bateria).

% Under Pressure
rol_requerido(voz_principal).
rol_requerido(voz_principal).  % <-- La segunda voz principal
rol_requerido(guitarra_electrica).
rol_requerido(bajo).
rol_requerido(bateria).


% ---
% --- REGLAS (RULES)
% ---

% 1. Regla: Un artista 'puede_cubrir' un rol si está en su lista de roles.
puede_cubrir(Artista, Rol) :-
    artista(Artista, Roles),
    member(Rol, Roles).

% 2. Regla: Un rol es 'cubierto_por_base' si al menos UN miembro base puede cubrirlo.
rol_cubierto_por_base(Rol) :-
    base_member(Artista),
    puede_cubrir(Artista, Rol).

% 3. Regla: Obtiene la LISTA ÚNICA de todos los roles requeridos en el recital.
%    - findall: Busca todos los 'Rol' que cumplen 'rol_requerido(Rol)' y los pone en una lista.
%    - list_to_set: Quita los duplicados.
todos_roles_requeridos(ListaUnicaRequeridos) :-
    findall(Rol, rol_requerido(Rol), RolesConDuplicados),
    list_to_set(RolesConDuplicados, ListaUnicaRequeridos).

% 4. Regla: Obtiene la LISTA ÚNICA de todos los roles cubiertos por los miembros base.
todos_roles_cubiertos_base(ListaUnicaCubiertos) :-
    findall(Rol, rol_cubierto_por_base(Rol), RolesConDuplicados),
    list_to_set(RolesConDuplicados, ListaUnicaCubiertos).

% 5. Regla: Calcula los roles faltantes.
%    - subtract: Es una resta de conjuntos. (Roles Requeridos) - (Roles Cubiertos)
roles_faltantes(ListaFaltantes) :-
    todos_roles_requeridos(Requeridos),
    todos_roles_cubiertos_base(Cubiertos),
    subtract(Requeridos, Cubiertos, ListaFaltantes).

% ---
% --- PREDICADO PRINCIPAL (Lo que llamaremos desde Java)
% ---

% Regla final: Los entrenamientos mínimos son la CANTIDAD de roles faltantes.
% - length: Calcula el largo de la lista.
entrenamientos_minimos(Cantidad) :-
    roles_faltantes(ListaRolesFaltantes),
    length(ListaRolesFaltantes, Cantidad).