import { useState, useEffect } from 'react';
import { doctorService, medicalServiceService } from '../services/api';
import { UserCog, Trash2, Edit2, Plus, CheckCircle2, AlertCircle, Loader2, Calendar } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';

export default function DoctorsPage() {
  const { user } = useAuth();
  const [doctors, setDoctors] = useState([]);
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [showPTOForm, setShowPTOForm] = useState(false);
  const [editId, setEditId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    office: '',
    numberOfPTOdays: 21,
    medicalServiceId: ''
  });
  const [ptoData, setPTOData] = useState({
    startDate: '',
    endDate: ''
  });

  const fetchData = async () => {
    try {
      const [doctorsRes, servicesRes] = await Promise.all([
        doctorService.getAll(),
        medicalServiceService.getAll()
      ]);
      setDoctors(doctorsRes.data);
      setServices(servicesRes.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching data:', error);
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleEdit = (doctor) => {
    setEditId(doctor.id);
    setFormData({
      name: doctor.name || '',
      office: doctor.office,
      numberOfPTOdays: doctor.numberOfPtodays,
      medicalServiceId: doctor.medicalService?.id?.toString() || ''
    });
    setShowForm(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage(null);
    try {
      const payload = {
        name: formData.name,
        office: formData.office,
        numberOfPtodays: parseInt(formData.numberOfPTOdays),
        medicalService: { id: parseInt(formData.medicalServiceId) }
      };

      if (editId) {
        await doctorService.update(editId, payload);
        setMessage({ type: 'success', text: 'Doctor actualizat cu succes!' });
      } else {
        await doctorService.create(payload);
        setMessage({ type: 'success', text: 'Doctor salvat cu succes!' });
      }

      setShowForm(false);
      setEditId(null);
      setFormData({ name: '', office: '', numberOfPTOdays: 21, medicalServiceId: '' });
      fetchData();
    } catch (error) {
      console.error('Error saving doctor:', error);
      setMessage({ 
        type: 'error', 
        text: 'Eroare la salvarea doctorului. Verificați datele introduse.' 
      });
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Ești sigur că vrei să ștergi acest doctor?')) {
      try {
        await doctorService.delete(id);
        setMessage({ type: 'success', text: 'Doctor șters cu succes!' });
        fetchData();
      } catch (error) {
        console.error('Error deleting doctor:', error);
        setMessage({ 
          type: 'error', 
          text: 'Nu s-a putut șterge doctorul. Asigurați-vă că acesta nu are programări active sau alte legături în sistem.' 
        });
      }
    }
  };

  const handlePTOSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const start = new Date(ptoData.startDate).toISOString();
      const end = new Date(ptoData.endDate).toISOString();
      await axios.post(`http://localhost:8080/api/doctor-schedule/schedulePTO`, null, {
        params: {
          doctorId: user.doctorId,
          startDate: start,
          endDate: end
        }
      });
      setMessage({ type: 'success', text: 'Concediu programat cu succes!' });
      setShowPTOForm(false);
      fetchData();
    } catch (error) {
      console.error('Error scheduling PTO:', error);
      setMessage({ type: 'error', text: error.response?.data || 'Eroare la programarea concediului.' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="w-full">
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-gray-800">Gestionare Doctori</h2>
        <div className="flex gap-2">
          {user.role === 'DOCTOR' && (
            <button 
              onClick={() => setShowPTOForm(!showPTOForm)}
              className="bg-orange-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-orange-700"
            >
              <Calendar size={20} />
              Programează Concediu
            </button>
          )}
          {user.role === 'DOCTOR' && (
        <button 
          onClick={() => {
            if (showForm && editId) {
              setEditId(null);
              setFormData({ name: '', office: '', numberOfPTOdays: 21, medicalServiceId: '' });
            } else {
              setShowForm(!showForm);
            }
          }}
          className="bg-indigo-600 text-white px-4 py-2 rounded-lg flex items-center gap-2 hover:bg-indigo-700"
        >
          <Plus size={20} />
          {editId ? 'Mod Nou Doctor' : 'Adaugă Doctor'}
        </button>
          )}
        </div>
      </div>

      {message && (
        <div className={`mb-6 p-4 rounded-lg flex items-center gap-3 animate-in fade-in duration-300 ${
          message.type === 'success' ? 'bg-green-50 text-green-700 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'
        }`}>
          {message.type === 'success' ? <CheckCircle2 size={20} /> : <AlertCircle size={20} />}
          <p className="font-medium">{message.text}</p>
        </div>
      )}

      {showPTOForm && (
        <form onSubmit={handlePTOSubmit} className="bg-orange-50 p-6 rounded-xl shadow-md mb-8 grid grid-cols-2 gap-4 border border-orange-200">
          <h3 className="col-span-2 text-lg font-bold text-orange-900 mb-2 border-b border-orange-200 pb-2">
            Programare Concediu (PTO)
          </h3>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">Dată Start</label>
            <input 
              type="datetime-local" 
              required
              className="border p-2 rounded-md focus:ring-2 focus:ring-orange-500 outline-none"
              value={ptoData.startDate}
              onChange={(e) => setPTOData({...ptoData, startDate: e.target.value})}
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">Dată Sfârșit</label>
            <input 
              type="datetime-local" 
              required
              className="border p-2 rounded-md focus:ring-2 focus:ring-orange-500 outline-none"
              value={ptoData.endDate}
              onChange={(e) => setPTOData({...ptoData, endDate: e.target.value})}
            />
          </div>
          <div className="col-span-2 flex justify-end gap-3 mt-2">
            <button 
              type="button" 
              onClick={() => setShowPTOForm(false)}
              className="text-gray-600 px-4 py-2 hover:bg-gray-100 rounded-lg"
            >
              Anulează
            </button>
            <button 
              type="submit" 
              className="bg-orange-600 text-white px-6 py-2 rounded-lg hover:bg-orange-700 shadow-md"
            >
              Confirmă Concediu
            </button>
          </div>
        </form>
      )}

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white p-6 rounded-xl shadow-md mb-8 grid grid-cols-2 gap-4 border border-indigo-100 ring-2 ring-indigo-500/10">
          <h3 className="col-span-2 text-lg font-bold text-indigo-900 mb-2 border-b pb-2">
            {editId ? `Editare Doctor #${editId}` : 'Adăugare Doctor Nou'}
          </h3>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">
              Nume Complet <span className="text-blue-600">*</span>
            </label>
            <input 
              type="text" 
              required
              disabled={saving}
              className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
              placeholder="ex: Dr. Popescu Ion"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">
              Cabinet / Oficiu <span className="text-blue-600">*</span>
            </label>
            <input 
              type="text" 
              required
              disabled={saving}
              className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none"
              value={formData.office}
              onChange={(e) => setFormData({...formData, office: e.target.value})}
              placeholder="ex: Cabinet 204"
            />
          </div>
          <div className="flex flex-col gap-1">
            <label className="text-sm font-semibold text-gray-600">
              Zile Concediu (PTO) <span className="text-blue-600">*</span>
            </label>
            <input 
              type="number" 
              required
              disabled={saving}
              className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none"
              value={formData.numberOfPTOdays}
              onChange={(e) => setFormData({...formData, numberOfPTOdays: e.target.value})}
            />
          </div>
          <div className="flex flex-col gap-1 col-span-2">
            <label className="text-sm font-semibold text-gray-600">
              Specializare / Serviciu Medical <span className="text-blue-600">*</span>
            </label>
            <select 
              required
              disabled={saving}
              className="border p-2 rounded-md focus:ring-2 focus:ring-indigo-500 outline-none"
              value={formData.medicalServiceId}
              onChange={(e) => setFormData({...formData, medicalServiceId: e.target.value})}
            >
              <option value="">Selectează un serviciu...</option>
              {services.map(service => (
                <option key={service.id} value={service.id}>
                  {service.name} ({service.specialization})
                </option>
              ))}
            </select>
          </div>
          <div className="col-span-2 flex justify-end gap-3 mt-2">
            <button 
              type="button" 
              onClick={() => {
                setShowForm(false);
                setEditId(null);
                setFormData({ name: '', office: '', numberOfPTOdays: 21, medicalServiceId: '' });
              }}
              disabled={saving}
              className="text-gray-600 px-4 py-2 hover:bg-gray-100 rounded-lg transition-colors"
            >
              Anulează
            </button>
            <button 
              type="submit" 
              disabled={saving}
              className="bg-indigo-600 text-white px-6 py-2 rounded-lg hover:bg-indigo-700 transition-all shadow-md disabled:opacity-70 flex items-center gap-2"
            >
              {saving ? (
                <>
                  <Loader2 className="animate-spin" size={18} />
                  Se salvează...
                </>
              ) : 'Salvează'}
            </button>
          </div>
        </form>
      )}

      {loading ? (
        <div className="text-center py-10">Se încarcă...</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {doctors.map((doctor) => (
            <div key={doctor.id} className="bg-white p-6 rounded-xl shadow-md border-l-4 border-indigo-500 hover:shadow-lg transition-shadow">
              <div className="flex justify-between items-start">
                <div>
                  <h3 className="text-xl font-bold text-gray-900">{doctor.name || `Dr. #${doctor.id}`}</h3>
                  <p className="text-indigo-600 font-medium">{doctor.medicalService?.name || 'Fără serviciu'}</p>
                </div>
                {user.role === 'DOCTOR' && (
                <div className="flex gap-2">
                  <button 
                    onClick={() => handleEdit(doctor)}
                    className="p-2 text-gray-400 hover:text-indigo-600 transition-colors"
                      title={user.doctorId === doctor.id ? "Editează datele tale" : "Editează doctor"}
                  >
                    <Edit2 size={18} />
                  </button>
                  <button 
                    onClick={() => handleDelete(doctor.id)}
                    className="p-2 text-gray-400 hover:text-red-600 transition-colors"
                      title={user.doctorId === doctor.id ? "Șterge contul tău de doctor" : "Șterge doctor"}
                  >
                    <Trash2 size={18} />
                  </button>
                </div>
                )}
              </div>
              
              <div className="mt-4 space-y-2 text-sm text-gray-600">
                <div className="flex justify-between">
                  <span>Cabinet:</span>
                  <span className="font-semibold text-gray-800">{doctor.office}</span>
                </div>
                <div className="flex justify-between">
                  <span>Zile PTO rămase:</span>
                  <span className="font-semibold text-gray-800">{doctor.numberOfPtodays}</span>
                </div>
                <div className="flex justify-between">
                  <span>Specializare:</span>
                  <span className="font-semibold text-gray-800">{doctor.medicalService?.specialization}</span>
                </div>
              </div>
            </div>
          ))}
          {doctors.length === 0 && (
            <div className="col-span-2 text-center py-10 text-gray-500">Nu există doctori înregistrați.</div>
          )}
        </div>
      )}
    </div>
  );
}

