import {useState, useEffect} from 'react';
import {Table, Typography, Button, Modal, Form, Input, message, Row, Col, Checkbox, Switch} from 'antd';
import axios from 'axios';
import {useKeycloak} from "@react-keycloak/web";
import {ColumnsType} from "antd/es/table";

const {Title} = Typography;

interface Product {
    id: number;
    name: string;
    description: string;
    pricePerDay: number;
    available: boolean;
    stock: number;
}

const Products = () => {
    const [data, setData] = useState<Product[]>([]);
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editingProduct, setEditingProduct] = useState<Product | null>(null);
    const [loading, setLoading] = useState(false);
    const [filters, setFilters] = useState({name: '', minPrice: '', maxPrice: ''});
    const [form] = Form.useForm();
    const {keycloak} = useKeycloak();

    const token = keycloak.token;
    const headers = {Authorization: `Bearer ${token}`};

    const fetchProducts = async (filters?: { name?: string; minPrice?: string; maxPrice?: string }) => {
        setLoading(true);
        try {
            const token = keycloak?.token;
            if (!token) {
                message.error("Token not found!");
                return;
            }

            const response = await axios.get<Product[]>(
                "http://localhost:8080/items/my",
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
            message.error("Failed to fetch products");
        } finally {
            setLoading(false);
        }
    };


    useEffect(() => {
        fetchProducts();
    }, [keycloak?.token]);

    const showModal = async (product: Product | null = null) => {
        setEditingProduct(product);
        setIsModalVisible(true);
        form.resetFields();
        if (product) {
            const productData = await fetchProductById(product.id);
            if (productData) {
                form.setFieldsValue({
                    name: productData.name,
                    description: productData.description,
                    pricePerDay: productData.pricePerDay,
                    stock: productData.stock,
                    available: productData.available,
                });
            }
        } else {
            form.resetFields();
        }
    };

    const fetchProductById = async (id: any) => {
        try {
            const response = await axios.get(`http://localhost:8080/items/${id}`, {headers});
            return response.data;
        } catch (error) {
            message.error(`Failed to fetch product details : ${error}`);
            return null;
        }
    };

    const handleSearch = () => {
        fetchProducts(filters);
    };

    const handleReset = () => {
        setFilters({ name: '', minPrice: '', maxPrice: '' });
        fetchProducts();
    };

    const handleSubmit = async () => {
        try {
            const values: Partial<Product> = await form.validateFields();
            const headers = {
                Authorization: `Bearer ${keycloak?.token}`,
            };
            if (editingProduct) {
                await axios.put(
                    `http://localhost:8080/items/${editingProduct.id}`,
                    values,
                    { headers }
                );
                message.success("Product updated successfully");
            } else {
                await axios.post("http://localhost:8080/items", values, {headers});
                message.success("Product added successfully");
            }

            fetchProducts();
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

    const handleDelete = async (id: any) => {
        try {
            await axios.delete(`http://localhost:8080/items/${id}`, {headers});
            message.success('Product deleted successfully');
            fetchProducts();
        } catch (error) {
            message.error(`Failed to delete product: ${error}`);
        }
    };

    const columns: ColumnsType<Product> = [
        {title: 'ID Product', dataIndex: 'id', key: 'id', sorter: (a, b) => a.id - b.id, defaultSortOrder: 'ascend'},
        {title: 'Name', dataIndex: 'name', key: 'name',sorter: (a, b) => a.name.localeCompare(b.name)},
        {title: 'Description', dataIndex: 'description', key: 'description'},
        {title: 'Price Per Day', dataIndex: 'pricePerDay', key: 'pricePerDay', sorter: (a, b) => a.pricePerDay - b.pricePerDay},
        {
            title: "Available",
            dataIndex: "available",
            key: "available",
            render: (available: boolean) => <Checkbox checked={available}/>,
        },
        {title: 'Stock', dataIndex: 'stock', key: 'stock', sorter: (a, b) => a.stock - b.stock},
        {
            title: 'Actions',
            key: 'actions',
            render: (_, record) => (
                <>
                    <Button onClick={() => showModal(record)} style={{marginRight: 8}}>Edit</Button>
                    <Button onClick={() => handleDelete(record.id)} danger>Delete</Button>
                </>
            ),
        },
    ];

    return (
        <div>
            {/* Header with Title and Add Product Button */}
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16}}>
                <Title level={3}>Products</Title>
                <Button type="primary" onClick={() => showModal()}>Add Product</Button>
            </div>

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
                        placeholder="Min Price"
                        type="number"
                        value={filters.minPrice}
                        onChange={(e) => setFilters({...filters, minPrice: e.target.value})}
                    />
                </Col>
                <Col span={6}>
                    <Input
                        placeholder="Max Price"
                        type="number"
                        value={filters.maxPrice}
                        onChange={(e) => setFilters({...filters, maxPrice: e.target.value})}
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
                title={editingProduct ? 'Edit Product' : 'Add Product'}
                visible={isModalVisible}
                onOk={handleSubmit}
                onCancel={handleCancel}
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        name="name"
                        label="Product Name"
                        rules={[{required: true, message: 'Please enter product name'}]}
                    >
                        <Input/>
                    </Form.Item>
                    <Form.Item
                        name="description"
                        label="Description"
                        rules={[{required: true, message: 'Please enter product description'}]}
                    >
                        <Input/>
                    </Form.Item>
                    <Form.Item
                        name="pricePerDay"
                        label="Price Per Day"
                        rules={[{required: true, message: 'Price Per Day'}]}
                    >
                        <Input type="number"/>
                    </Form.Item>
                    <Form.Item
                        name="available"
                        label="Available"
                        valuePropName="checked"
                    >
                        <Switch/>
                    </Form.Item>
                    <Form.Item
                        name="stock"
                        label="Stock"
                        rules={[{required: true, message: 'Stock'}]}
                    >
                        <Input type="number"/>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default Products;