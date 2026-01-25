import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { LogIn, Lock, User as UserIcon } from 'lucide-react';
import { useNavigate, Link, useLocation } from 'react-router-dom';

export default function LoginPage() {
  const location = useLocation();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const successMessage = location.state?.message;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      setError('Autentificare eșuată. Verifică utilizatorul și parola.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-2xl shadow-xl border border-gray-100">
        <div>
          <div className="mx-auto h-16 w-16 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center">
            <LogIn size={32} />
          </div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Autentificare MedManager
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Introdu datele pentru a accesa platforma
          </p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {successMessage && (
            <div className="bg-green-50 text-green-700 p-3 rounded-lg text-sm text-center border border-green-200">
              {successMessage}
            </div>
          )}
          {error && (
            <div className="bg-red-50 text-red-700 p-3 rounded-lg text-sm text-center border border-red-200">
              {error}
            </div>
          )}
          <div className="rounded-md shadow-sm -space-y-px">
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <UserIcon size={18} />
              </div>
              <input
                type="text"
                required
                className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="Nume utilizator"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </div>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <Lock size={18} />
              </div>
              <input
                type="password"
                required
                className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="Parolă"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-bold rounded-lg text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition-colors shadow-md"
            >
              {loading ? 'Se autentifică...' : 'Conectare'}
            </button>
          </div>

          <div className="text-center">
            <Link to="/register" className="text-sm font-medium text-indigo-600 hover:text-indigo-500">
              Nu ai un cont? Înregistrează-te acum
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}

