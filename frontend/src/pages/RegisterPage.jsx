import React, { useState, useEffect } from 'react';
import { UserPlus, Lock, User as UserIcon, ShieldCheck, Calendar, Users, Building2, Briefcase } from 'lucide-react';
import { useNavigate, Link } from 'react-router-dom';
import api, { medicalServiceService } from '../services/api';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [age, setAge] = useState('');
  const [sex, setSex] = useState(true);
  const [role, setRole] = useState('PATIENT');
  const [office, setOffice] = useState('');
  const [numberOfPTOdays, setNumberOfPTOdays] = useState(21);
  const [medicalServiceId, setMedicalServiceId] = useState('');
  const [medicalServices, setMedicalServices] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    // Load medical services when component mounts or when role changes to DOCTOR
    medicalServiceService.getAll()
      .then(response => {
        if (response.data && Array.isArray(response.data)) {
          setMedicalServices(response.data);
        }
      })
      .catch(err => {
        console.error('Error fetching medical services:', err);
        setError('Nu s-au putut încărca specializările. Te rugăm să reîmprospătezi pagina.');
      });
  }, []);

  const calculateAge = (birthDate) => {
    if (!birthDate) return null;
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const monthDiff = today.getMonth() - birth.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    
    // Validate age for patients
    if (role === 'PATIENT' && age) {
      const patientAge = calculateAge(age);
      if (patientAge === null || patientAge < 18) {
        setError('Pacienții trebuie să aibă minim 18 ani pentru a se înregistra.');
        setLoading(false);
        return;
      }
    }
    
    try {
      const payload = {
        username, 
        password,
        fullName,
        age: age || null,
        sex,
        role
      };
      
      if (role === 'PATIENT') {
        payload.age = age || null;
        payload.sex = sex;
      } else if (role === 'DOCTOR') {
        payload.office = office;
        payload.numberOfPTOdays = parseInt(numberOfPTOdays);
        payload.medicalServiceId = medicalServiceId ? parseInt(medicalServiceId) : null;
      }
      
      await api.post('http://localhost:8080/auth/register', payload);
      navigate('/login', { state: { message: 'Cont creat cu succes! Te poți autentifica acum.' } });
    } catch (err) {
      const errorMessage = typeof err.response?.data === 'string' 
        ? err.response.data 
        : 'Eroare la crearea contului. Te rugăm să încerci din nou.';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-2xl shadow-xl border border-gray-100">
        <div>
          <div className="mx-auto h-16 w-16 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center">
            <UserPlus size={32} />
          </div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Creează Cont MedManager
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Alătură-te platformei noastre de management medical
          </p>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
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
                placeholder="Nume utilizator (Login)"
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
                className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="Parolă"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <UserIcon size={18} />
              </div>
              <input
                type="text"
                required
                className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                placeholder="Nume Complet"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
              />
            </div>

            {role === 'PATIENT' && (
              <>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                    <Calendar size={18} />
                  </div>
                  <input
                    type="date"
                    required
                    className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                    placeholder="Data Nașterii"
                    value={age}
                    onChange={(e) => setAge(e.target.value)}
                    max={new Date(new Date().setFullYear(new Date().getFullYear() - 18)).toISOString().split('T')[0]}
                  />
                </div>

                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                    <Users size={18} />
                  </div>
                  <select
                    className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm bg-white"
                    value={sex}
                    onChange={(e) => setSex(e.target.value === 'true')}
                  >
                    <option value="true">Masculin</option>
                    <option value="false">Feminin</option>
                  </select>
                </div>
              </>
            )}

            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <ShieldCheck size={18} />
              </div>
              <select
                className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm bg-white"
                value={role}
                onChange={(e) => setRole(e.target.value)}
              >
                <option value="PATIENT">Pacient</option>
                <option value="DOCTOR">Doctor</option>
              </select>
            </div>
            
            {role === 'DOCTOR' && (
              <>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                    <Building2 size={18} />
                  </div>
                  <input
                    type="text"
                    required
                    className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                    placeholder="Cabinet / Oficiu"
                    value={office}
                    onChange={(e) => setOffice(e.target.value)}
                  />
                </div>
                
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                    <Calendar size={18} />
                  </div>
                  <input
                    type="number"
                    required
                    min="0"
                    className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
                    placeholder="Zile Concediu (PTO)"
                    value={numberOfPTOdays}
                    onChange={(e) => setNumberOfPTOdays(e.target.value)}
                  />
                </div>
                
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                    <Briefcase size={18} />
                  </div>
                  <select
                    required
                    className="appearance-none rounded-none relative block w-full px-10 py-3 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm bg-white"
                    value={medicalServiceId}
                    onChange={(e) => setMedicalServiceId(e.target.value)}
                    disabled={loading || medicalServices.length === 0}
                  >
                    <option value="">Selectează Specializarea</option>
                    {medicalServices.map(service => (
                      <option key={service.id} value={service.id}>
                        {service.name} ({service.specialization})
                      </option>
                    ))}
                  </select>
                  {medicalServices.length === 0 && !loading && (
                    <p className="text-xs text-gray-500 mt-1 px-10">Se încarcă specializările...</p>
                  )}
                </div>
              </>
            )}
          </div>

          <div>
            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-bold rounded-lg text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition-colors shadow-md"
            >
              {loading ? 'Se creează contul...' : 'Înregistrare'}
            </button>
          </div>

          <div className="text-center">
            <Link to="/login" className="text-sm font-medium text-indigo-600 hover:text-indigo-500">
              Ai deja un cont? Autentifică-te
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}

