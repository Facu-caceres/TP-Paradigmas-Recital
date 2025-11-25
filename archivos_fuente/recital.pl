% --- Predicados dinámicos cargados desde Java ---
:- dynamic base_member/1.
:- dynamic artista/2.
:- dynamic rol_requerido/1.


% ---
% --- REGLAS (RULES)
% ---

% 1. Un artista 'puede_cubrir' un rol si está en su lista de roles.
puede_cubrir(Artista, Rol) :-
    artista(Artista, Roles),
    member(Rol, Roles).

% 2. Un rol es cubierto si al menos un artista disponible puede tocarlo.
rol_cubierto(Rol) :-
    puede_cubrir(_, Rol).

% 3. Lista única de roles requeridos en el recital.
todos_roles_requeridos(ListaUnicaRequeridos) :-
    findall(Rol, rol_requerido(Rol), RolesConDuplicados),
    list_to_set(RolesConDuplicados, ListaUnicaRequeridos).

% 4. Lista única de roles que ya puede cubrir el conjunto actual de artistas.
todos_roles_cubiertos(ListaUnicaCubiertos) :-
    findall(Rol, rol_cubierto(Rol), RolesConDuplicados),
    list_to_set(RolesConDuplicados, ListaUnicaCubiertos).

% 5. Calcula los roles faltantes en función de todo el plantel disponible.
roles_faltantes(ListaFaltantes) :-
    todos_roles_requeridos(Requeridos),
    todos_roles_cubiertos(Cubiertos),
    subtract(Requeridos, Cubiertos, ListaFaltantes).

% ---
% --- PREDICADO PRINCIPAL (Lo que llamaremos desde Java)
% ---

% Regla final: Los entrenamientos mínimos son la cantidad de roles faltantes.
entrenamientos_minimos(Cantidad) :-
    roles_faltantes(ListaRolesFaltantes),
    length(ListaRolesFaltantes, Cantidad).