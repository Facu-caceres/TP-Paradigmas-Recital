% --- Predicados dinámicos cargados desde Java ---
:- dynamic base_member/1.
:- dynamic artista/2.
:- dynamic rol_requerido/1.


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