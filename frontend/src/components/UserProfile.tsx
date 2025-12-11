import { useState, useEffect } from 'react';

interface User {
  id: number;
  email: string;
  nombre: string;
  nombrePila: string;
  apellido: string;
  fotoUrl: string;
}

export default function UserProfile() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/user/me', { credentials: 'include' })
      .then(res => {
        if (res.ok) return res.json();
        throw new Error('No autenticado');
      })
      .then(data => setUser(data))
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const handleLogout = async () => {
    await fetch('/api/user/logout', {
      method: 'POST',
      credentials: 'include'
    });
    setUser(null);
    window.location.reload();
  };

  if (loading) {
    return <div style={styles.loading}>Cargando...</div>;
  }

  if (!user) {
    return (
      <a href="/api/auth/oauth2/authorization/google" style={styles.googleBtn}>
        <svg viewBox="0 0 24 24" style={styles.googleIcon}>
          <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
          <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
          <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
          <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
        </svg>
        Iniciar sesión con Google
      </a>
    );
  }

  return (
    <div style={styles.profile}>
      <img
        src={user.fotoUrl}
        alt={user.nombre}
        style={styles.avatar}
      />
      <div style={styles.info}>
        <h3 style={styles.name}>{user.nombre}</h3>
        <p style={styles.email}>{user.email}</p>
      </div>
      <button onClick={handleLogout} style={styles.logoutBtn}>
        Cerrar sesión
      </button>
    </div>
  );
}

const styles: { [key: string]: React.CSSProperties } = {
  loading: {
    color: '#999',
    fontSize: '14px',
  },
  googleBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    background: '#fff',
    color: '#333',
    border: 'none',
    padding: '12px 24px',
    borderRadius: '4px',
    fontSize: '16px',
    cursor: 'pointer',
    textDecoration: 'none',
    marginTop: '20px',
    transition: 'box-shadow 0.2s',
  },
  googleIcon: {
    width: '20px',
    height: '20px',
  },
  profile: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: '15px',
    padding: '20px',
    background: 'rgba(255,255,255,0.1)',
    borderRadius: '12px',
    marginTop: '20px',
  },
  avatar: {
    width: '80px',
    height: '80px',
    borderRadius: '50%',
    border: '3px solid #ff6b9d',
  },
  info: {
    textAlign: 'center',
  },
  name: {
    margin: '0',
    color: '#fff',
    fontSize: '1.2rem',
  },
  email: {
    margin: '5px 0 0',
    color: '#aaa',
    fontSize: '0.9rem',
  },
  logoutBtn: {
    background: 'transparent',
    border: '1px solid #ff6b9d',
    color: '#ff6b9d',
    padding: '8px 16px',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
};
