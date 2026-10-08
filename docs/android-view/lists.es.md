# Listas

Para `RecyclerView`, Ema da adapters basados en `ListAdapter` y ViewBinding.

## Un tipo de elemento

```kotlin
class HomeSingleAdapter : EmaRecyclerAdapter<HomeLayoutItemUserBinding, User>() {

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        HomeLayoutItemUserBinding.inflate(inflater, container, false)

    override fun HomeLayoutItemUserBinding.bind(
        item: User,
        viewType: Int,
        holder: EmaViewHolder<User>,
        payloads: MutableList<Any>
    ) {
        tvHomeItemUserAvatar.text = item.initials
        tvHomeItemUser.text = item.fullName
    }
}
```

## Varios tipos de elemento

Hereda de `EmaMultiRecyclerAdapter`, devuelve el tipo en `getItemViewType` y crea el binding de cada tipo:

```kotlin
class HomeMultiAdapter : EmaMultiRecyclerAdapter<User>() {

    override fun getItemViewType(position: Int) = currentList[position].role.ordinal

    override fun createMultiViewHolder(view: ViewGroup, viewType: Int): EmaAdapterMultiViewHolder {
        val inflater = LayoutInflater.from(view.context)
        val binding = when (Role.entries[viewType]) {
            Role.ADMIN -> HomeLayoutItemAdminBinding.inflate(inflater, view, false)
            Role.BASIC -> HomeLayoutItemUserBinding.inflate(inflater, view, false)
        }
        return EmaAdapterMultiViewHolder(binding, viewType)
    }

    override fun ViewBinding.bind(item: User, viewType: Int, holder: EmaViewHolder<User>, payloads: MutableList<Any>) {
        when (Role.entries[viewType]) {
            Role.ADMIN -> (this as HomeLayoutItemAdminBinding).apply { /* ... */ }
            Role.BASIC -> (this as HomeLayoutItemUserBinding).apply { /* ... */ }
        }
    }
}
```

## Alimentar el adapter

Aliméntalo desde el estado, con `submitList`, y deja que `DiffUtil` anime los cambios:

```kotlin
bindForUpdate(state::userList) {
    adapter.submitList(it)
}
```

Por defecto, dos elementos son el mismo si son iguales (`equals`). Pasa tu propio `DiffUtil.ItemCallback` al constructor
para compararlos por id, o usa `EmaBaseRecyclerAdapter.getAlwaysUpdateCallback()` para repintar siempre.

## Clics

```kotlin
adapter.setOnItemClickListener { index, item ->
    viewModel.dispatch(HomeAction.UserClicked(item))
}
```

Envía el clic como una acción. Cambia la lista en el estado y envía la nueva: `updateList`, `addItem`, `removeItem` y
`updateItem` también existen, pero entonces el adapter y el estado dejan de mostrar la misma lista.

## Decoraciones

| Clase                           | Para                                                                                |
|---------------------------------|-------------------------------------------------------------------------------------|
| `EmaVerticalSpaceDecoration`    | Espacio entre los elementos de una lista vertical, opcionalmente también tras el último. |
| `EmaGridSpacingItemDecoration`  | Espacio entre las columnas y las filas de una cuadrícula, opcionalmente en los bordes. |
| `EmaSwipeToDeleteCallback`      | Deslizar para borrar con `ItemTouchHelper`: pinta un fondo y un icono o un texto bajo el elemento. |

`RecyclerView.clearAdapters()` quita el adapter, para evitar fugas de memoria cuando se destruye la vista.
