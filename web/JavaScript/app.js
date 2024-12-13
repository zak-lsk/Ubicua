function enviarAccion(seccion, accion) {
    fetch(`/SisdogarControlador`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ seccion: seccion, accion: accion })
    })
      .then(response => response.json())
      .then(data => alert(data.mensaje))
      .catch(error => console.error('Error:', error));
  }
  
