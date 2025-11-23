import {
    Badge,
    Button,
    Checkbox,
    Col,
    DatePicker,
    Form,
    Input,
    message,
    Modal,
    Row,
    Select, Switch,
    Table,
    Typography
} from "antd";
import {useEffect, useState} from "react";
import {useKeycloak} from "@react-keycloak/web";
import {ColumnsType} from "antd/es/table";
import axios from "axios";

const {Title} = Typography;

interface Rental {
    id: number;
    itemId: number;
    borrowerId: string;
    borrowerName: string;
    startDate: string;
    endDate: string;
    totalPrice: number;
    status: string;
    approvedBy: string;
    paid: boolean;
}

interface Product {
    id: number;
    name: string;
    description: string;
    pricePerDay: number;
    available: boolean;
    stock: number;
}

const RequestRentals = () => {
    const [data, setData] = useState<Rental[]>([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingRental, setEditingRental] = useState<Rental | null>(null);
    const [products, setProducts] = useState<Product[]>([]);
    const [loading, setLoading] = useState(false);
    const [filters, setFilters] = useState({name: '', status: ''});
    const [form] = Form.useForm();
    const {keycloak} = useKeycloak();

    const token = keycloak.token;
    const headers = {Authorization: `Bearer ${token}`};

    useEffect(() => {
        fetchRentals();
        fetchProducts();
    }, [keycloak?.token]);

    const statusColors: Record<string, string> = {
        PENDING: "warning",
        APPROVED: "blue",
        ONGOING: "processing",
        COMPLETED: "success",
        CANCELLED: "error"
    };

    const calculateDays = (start, end) => {
        return end.diff(start, "day") + 1;
    };

    const fetchProducts = async () => {
        try {
            const response = await axios.get("http://localhost:8080/items", {headers});
            setProducts(response.data);
        } catch (error) {
            message.error(`Failed to fetch products : ${error}`);
        }
    };

    const fetchRentals = async (filters?: { name?: string; status?: string }) => {
        setLoading(true);
        try {
            const token = keycloak?.token;
            if (!token) {
                message.error("Token not found!");
                return;
            }

            const response = await axios.get<Rental[]>(
                "http://localhost:8080/rentals/request",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                    params: filters
                }
            );

            const sortedData = (response.data ?? []).sort((a, b) => a.id - b.id);
            setData(sortedData);

        } catch (error) {
            console.error(error);
            message.error("Failed to fetch rentals");
        } finally {
            setLoading(false);
        }
    };

    const fetchRentalById = async (id: any) => {
        try {
            const response = await axios.get(`http://localhost:8080/rentals/${id}`, {headers});
            console.log(response)
            return response.data;
        } catch (error) {
            message.error(`Failed to fetch rental details : ${error}`);
            return null;
        }
    };

    const showModal = async (rental: Rental | null = null) => {
        setEditingRental(rental);
        setIsModalVisible(true);
        form.resetFields();
        if (rental) {
            const rentalData = await fetchRentalById(rental.id);
            if (rentalData) {
                form.setFieldsValue({
                    itemId: rentalData.itemId,
                    itemName: rentalData.itemName,
                    borrowerId: rentalData.borrowerId,
                    borrowerName: rentalData.borrowerName,
                    startDate: rentalData.startDate,
                    endDate: rentalData.endDate,
                    totalPrice: rentalData.totalPrice,
                    status: rentalData.status,
                    approvedBy: rentalData.approvedBy,
                    paid: rentalData.paid,
                });
            }
        } else {
            form.resetFields();
        }
    };

    const handleDelete = async (id: any) => {
        try {
            await axios.delete(`http://localhost:8080/rentals/${id}`, {headers});
            message.success('Rental deleted successfully');
            fetchRentals();
        } catch (error) {
            message.error(`Failed to delete rental: ${error}`);
        }
    };

    const handleSubmit = async () => {
        try {
            const values: Partial<Product> = await form.validateFields();
            const headers = {
                Authorization: `Bearer ${keycloak?.token}`,
            };
            await axios.post("http://localhost:8080/rentals", values, {headers});
            message.success("Rental added successfully");

            fetchRentals();
            handleCancel();
        } catch (e: any) {
            if (axios.isAxiosError(e) && e.response) {
                message.error(e.response.data?.message ?? "Something went wrong");
            } else {
                message.error("Failed to save product");
            }
        }
    };

    const handleCancel = () => {
        setIsModalVisible(false);
        form.resetFields();
    };

    const handleSearch = () => {
        fetchRentals(filters);
    };

    const handleReset = () => {
        setFilters({name: '', status: ''});
        fetchRentals();
    };

    const columns: ColumnsType<Rental> = [
        {title: 'ID Rental', dataIndex: 'id', key: 'id'},
        {title: 'Borrower Name', dataIndex: 'borrowerName', key: 'borrowerName'},
        {title: 'Product', dataIndex: 'itemName', key: 'itemName'},
        {title: 'Start Date', dataIndex: 'startDate', key: 'startDate'},
        {title: 'End Date', dataIndex: 'endDate', key: 'endDate'},
        {
            title: 'Total Price',
            dataIndex: 'totalPrice',
            key: 'totalPrice',
            sorter: (a, b) => a.totalPrice - b.totalPrice
        },
        {
            title: 'Status',
            dataIndex: 'status',
            key: 'status',
            render: (status: string) => (
                <Badge count={status} showZero color={statusColors[status] || "default"}/>
            ),
        },
        {
            title: "Paid",
            dataIndex: "paid",
            key: "paid",
            render: (available: boolean) => <Checkbox checked={available}/>,
        },
        {
            title: 'Actions',
            key: 'actions',
            render: (_, record) => (
                <>
                    <Button onClick={() => showModal(record)} style={{marginRight: 8}}>Update</Button>
                    <Button onClick={() => handleDelete(record.id)} danger>Delete</Button>
                </>
            ),
        },
    ];

    return (
        <div>
            {/* Search Filters */}
            <Row gutter={16} style={{marginBottom: 16, display: 'flex', alignItems: 'center'}}>
                <Col span={6}>
                    <Input
                        placeholder="Search by name"
                        value={filters.name}
                        onChange={(e) => setFilters({...filters, name: e.target.value})}
                    />
                </Col>
                <Col span={6}>
                    <Input
                        placeholder="Search by status"
                        value={filters.status}
                        onChange={(e) => setFilters({...filters, status: e.target.value})}
                    />
                </Col>
                <Col span={6} style={{display: 'flex', gap: '8px'}}>
                    <Button type="primary" onClick={handleSearch}>Search</Button>
                    <Button onClick={handleReset}>Reset</Button>
                </Col>
            </Row>

            {/* Products Table */}
            <Table
                columns={columns}
                dataSource={data}
                rowKey="id"
                loading={loading}
                pagination={false}
            />

            {/* Modal for Add/Edit Product */}
            <Modal
                title={editingRental ? 'Update Rental' : 'Add Rental'}
                visible={isModalVisible}
                onOk={handleSubmit}
                onCancel={handleCancel}
                bodyStyle={{
                    maxHeight: '60vh',
                    overflowY: 'auto'
                }}
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        name="itemName"
                        label="Product Name">
                        <Input readOnly/>
                    </Form.Item>
                    <Form.Item
                        name="borrowerName"
                        label="Borrower Name">
                        <Input readOnly/>
                    </Form.Item>
                    <Form.Item
                        name="startDate"
                        label="Start Date"
                        rules={[{required: true, message: 'Please enter Start Date'}]}>
                        <Input readOnly/>
                    </Form.Item>
                    <Form.Item
                        name="endDate"
                        label="End Date"
                        rules={[{required: true, message: 'Please enter End Date'}]} readOnly>
                        <Input/>
                    </Form.Item>
                    <Form.Item
                        name="status"
                        label="Status">
                        <Input readOnly/>
                    </Form.Item>
                    <Form.Item
                        name="paid"
                        label="Paid"
                        valuePropName="checked"
                    >
                        <Switch/>
                    </Form.Item>
                    <Form.Item
                        name="totalPrice"
                        label="Total Price">
                        <Input type="number" readOnly/>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default RequestRentals;