import { useState, useEffect } from 'react';
import { patientService } from '../services/api';
import { UserPlus, Trash2, Edit2 } from 'lucide-react';

export default function PatientsPage() {
  const [patients, setPatients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    age: '',
    medicalRecord: '',
    insurance: false,
    subscription: false,
    sex: true // true for Male, false for Female (based on Entity Boolean sex)
  });

  const fetchPatients = async () => {
    try {
      const response = await patientService.getAll();
      setPatients(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching patients:', error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPatients();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await patientService.create({
        ...formData,
        age: parseInt(formData.age)
      });
      setShowForm(false);
      setFormData({ name: '', age: '', medicalRecord: '', insurance: false, subscription: false, sex: true });
      fetchPatients();
    } catch (error) {
      console.error('Error creating patient:', error);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Ești sigur că vrei să ștergi acest pacient?')) {
      try {
        await patientService.delete(id);
        fetchPatients();
      } catch (error) {
        console.error('Error deleting patient:', error);
      }
    }
  };

  return (
    <div className="w-full">
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-gray-800">Gestionare Pacienți</h2>
        <button 
          onClick={() => setShowForm(!showForm)}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-indigo-700"
        >
          <UserPlus size={20} />
          Adaugă Pacient
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-6 rounded-xl shadow-md mb-8 grid grid-cols-2 gap-4">
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">Nume Complet</label>
            <input 
              type="text" 
              required
              className="border p-2 rounded-md"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">Vârstă</label>
            <input 
              type="number" 
              required
              className="border p-2 rounded-md"
              value={formData.age}
              onChange={(e) => setFormData({...formData, age: e.target.value})}
            />
          </div>
          <div className="flex flex-col gap-1 col-span-2">
            <label className="text-sm font-semibold text-gray-600">Istoric Medical</label>
            <textarea 
              className="border p-2 rounded-md h-24"
              value={formData.medicalRecord}
              onChange={(e) => setFormData({...formData, medicalRecord: e.target.value})}
            />
          </div>
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input 
                type="checkbox" 
                checked={formData.insurance}
                onChange={(e) => setFormData({...formData, insurance: e.target.checked})}
              />
              Asigurare
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input 
                type="checkbox" 
                checked={formData.subscription}
                onChange={(e) => setFormData({...formData, subscription: e.target.checked})}
              />
              Abonament
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <select 
                className="border p-1 rounded"
                value={formData.sex ? 'true' : 'false'}
                onChange={(e) => setFormData({...formData, sex: e.target.value === 'true'})}
              >
                <option value="true">Masculin</option>
                <option value="false">Feminin</option>
              </select>
            </label>
          </div>
          <div className="col-span-2 flex justify-end gap-3 mt-2">
            <button 
              type="button" 
              onClick={() => setShowForm(false)}
              className="text-gray-600 px-4 py-2 hover:bg-gray-100 rounded-lg"
            >
              Anulează
            </button>
            <button 
              type="submit" 
              className="bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700"
            >
              Salvează
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="text-center py-10">Se încarcă...</div>
      ) : (
        <div className="bg-white rounded-xl shadow-md overflow-hidden">
          <table className="w-full text-left">
            <thead className="bg-gray-50 border-b">
              <tr>
                <th className="px-6 py-4 font-semibold text-gray-700">ID</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Nume</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Vârstă</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Sex</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Asigurare</th>
                <th className="px-6 py-4 font-semibold text-gray-700">Acțiuni</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {patients.map((patient) => (
                <tr key={patient.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 text-gray-600">#{patient.id}</td>
                  <td className="px-6 py-4 font-medium text-gray-900">{patient.name}</td>
                  <td className="px-6 py-4 text-gray-600">{patient.age} ani</td>
                  <td className="px-6 py-4 text-gray-600">{patient.sex ? 'M' : 'F'}</td>
                  <td className="px-6 py-4">
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${patient.insurance ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                      {patient.insurance ? 'Da' : 'Nu'}
                    </span>
                  </td>
                  <td className="px-6 py-4 flex gap-3">
                    <button className="text-indigo-600 hover:text-indigo-900"><Edit2 size={18} /></button>
                    <button 
                      onClick={() => handleDelete(patient.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                      <Trash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {patients.length === 0 && (
            <div className="text-center py-10 text-gray-500">Nu există pacienți înregistrați.</div>
          )}
        </div>
      )}
    </div>
  );
}

