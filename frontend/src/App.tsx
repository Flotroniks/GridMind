import { useEffect, useState } from 'react'
import './App.css'

interface Item {
  id: number
  name: string
  quantity: number
}

function App() {
  const [items, setItems] = useState<Item[]>([])

  useEffect(() => {
    fetch('http://localhost:8080/api/items')
      .then((res) => res.json())
      .then(setItems)
  }, [])

  return (
    <>
      <h1>GridMind</h1>
      <ul>
        {items.map((item) => (
          <li key={item.id}>
            {item.name} — {item.quantity}
          </li>
        ))}
      </ul>
    </>
  )
}

export default App
